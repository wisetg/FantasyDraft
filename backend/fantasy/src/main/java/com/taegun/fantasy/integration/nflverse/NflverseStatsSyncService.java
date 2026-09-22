package com.taegun.fantasy.integration.nflverse;

import com.taegun.fantasy.player.Player;
import com.taegun.fantasy.player.PlayerExternalId;
import com.taegun.fantasy.player.PlayerExternalIdRepository;
import com.taegun.fantasy.player.PlayerProvider;
import com.taegun.fantasy.player.PlayerStats;
import com.taegun.fantasy.player.PlayerStatsRepository;
import com.taegun.fantasy.player.ScoringFormat;

import com.taegun.fantasy.service.FantasyScoreService;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;

import java.time.Duration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * nflverse Player Weekly Stats를
 * PlayerStats 테이블과 동기화합니다.
 *
 * 지원 포지션:
 *
 * QB
 * RB
 * WR
 * TE
 * K
 */
@Service
public class NflverseStatsSyncService {

    private static final Set<String>
            SUPPORTED_POSITIONS =
            Set.of(
                    "QB",
                    "RB",
                    "WR",
                    "TE",
                    "K"
            );


    private static final String
            NFLVERSE_URL_FORMAT =
            "https://github.com/"
                    + "nflverse/nflverse-data/"
                    + "releases/download/"
                    + "stats_player/"
                    + "stats_player_week_%d.csv";


    private static final String
            NFLVERSE_PBP_URL_FORMAT =
            "https://github.com/"
                    + "nflverse/nflverse-data/"
                    + "releases/download/"
                    + "pbp/"
                    + "play_by_play_%d.csv";


    private final PlayerExternalIdRepository
            playerExternalIdRepository;

    private final PlayerStatsRepository
            playerStatsRepository;

    private final FantasyScoreService
            fantasyScoreService;

    private final HttpClient
            httpClient;


    public NflverseStatsSyncService(
            PlayerExternalIdRepository playerExternalIdRepository,
            PlayerStatsRepository playerStatsRepository,
            FantasyScoreService fantasyScoreService
    ) {

        this.playerExternalIdRepository =
                playerExternalIdRepository;

        this.playerStatsRepository =
                playerStatsRepository;

        this.fantasyScoreService =
                fantasyScoreService;


        this.httpClient =
                HttpClient
                        .newBuilder()

                        .connectTimeout(
                                Duration.ofSeconds(15)
                        )

                        .followRedirects(
                                HttpClient.Redirect.NORMAL
                        )

                        .build();
    }


    /**
     * 한 시즌의 Regular Season Player Stats를
     * 우리 DB와 동기화합니다.
     */
    @Transactional
    public NflverseStatsSyncResult syncSeason(
            int season
    ) {

        try {

            // =====================================================
            // 1. nflverse CSV 다운로드
            // =====================================================

            String url =
                    NFLVERSE_URL_FORMAT
                            .formatted(
                                    season
                            );


            HttpRequest request =
                    HttpRequest
                            .newBuilder()

                            .uri(
                                    URI.create(url)
                            )

                            .timeout(
                                    Duration.ofSeconds(90)
                            )

                            .GET()

                            .build();


            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse
                                    .BodyHandlers
                                    .ofString()
                    );


            if (
                    response.statusCode() < 200
                            ||
                            response.statusCode() >= 300
            ) {

                throw new RuntimeException(
                        "nflverse stats request failed. status="
                                + response.statusCode()
                                + ", season="
                                + season
                );
            }


            // =====================================================
            // 2. GSIS ID → 우리 Player
            // =====================================================

            List<PlayerExternalId> gsisMappings =
                    playerExternalIdRepository
                            .findByProvider(
                                    PlayerProvider.GSIS
                            );


            Map<String, Player> playerByGsis =
                    new HashMap<>();


            for (
                    PlayerExternalId mapping
                    :
                    gsisMappings
            ) {

                playerByGsis.put(
                        mapping.getExternalId(),
                        mapping.getPlayer()
                );
            }


            // =====================================================
            // 3. 기존 시즌 Stats 읽기
            // =====================================================

            List<PlayerStats> existingStats =
                    playerStatsRepository
                            .findBySeason(
                                    season
                            );


            Map<String, PlayerStats> existingStatsByKey =
                    new HashMap<>();


            for (
                    PlayerStats stats
                    :
                    existingStats
            ) {

                existingStatsByKey.put(
                        createKey(
                                stats
                                        .getPlayer()
                                        .getId(),
                                stats.getWeek()
                        ),
                        stats
                );
            }


            // =====================================================
            // 4. CSV Parser
            // =====================================================

            CSVFormat format =
                    CSVFormat.DEFAULT
                            .builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .get();


            int sourceRows = 0;

            int regularSeasonRows = 0;

            int matchedRows = 0;

            int createdStats = 0;

            int updatedStats = 0;

            int skippedNoGsisMatch = 0;

            int skippedUnsupportedPosition = 0;


            Map<String, PlayerStats> statsToSave =
                    new LinkedHashMap<>();


            try (
                    CSVParser parser =
                            CSVParser.parse(
                                    response.body(),
                                    format
                            )
            ) {

                for (
                        CSVRecord record
                        :
                        parser
                ) {

                    sourceRows++;


                    // =================================================
                    // Regular Season만 사용
                    // =================================================

                    String seasonType =
                            getString(
                                    record,
                                    "season_type"
                            );


                    if (
                            !"REG".equalsIgnoreCase(
                                    seasonType
                            )
                    ) {

                        continue;
                    }


                    regularSeasonRows++;


                    // =================================================
                    // Position
                    // =================================================

                    String position =
                            getString(
                                    record,
                                    "position"
                            );


                    if (
                            position == null
                                    ||
                                    !SUPPORTED_POSITIONS
                                            .contains(
                                                    position.toUpperCase()
                                            )
                    ) {

                        skippedUnsupportedPosition++;

                        continue;
                    }


                    // =================================================
                    // GSIS Player ID
                    // =================================================

                    String gsisId =
                            getString(
                                    record,
                                    "player_id"
                            );


                    if (
                            gsisId == null
                    ) {

                        skippedNoGsisMatch++;

                        continue;
                    }


                    Player player =
                            playerByGsis
                                    .get(
                                            gsisId
                                    );


                    if (
                            player == null
                    ) {

                        skippedNoGsisMatch++;

                        continue;
                    }


                    matchedRows++;


                    // =================================================
                    // Season / Week
                    // =================================================

                    int rowSeason =
                            getInt(
                                    record,
                                    "season"
                            );


                    if (
                            rowSeason != season
                    ) {

                        continue;
                    }


                    int week =
                            getInt(
                                    record,
                                    "week"
                            );


                    if (
                            week <= 0
                    ) {

                        continue;
                    }


                    // =================================================
                    // Passing
                    // =================================================

                    int passingYards =
                            getInt(
                                    record,
                                    "passing_yards"
                            );


                    int passingTouchdowns =
                            getInt(
                                    record,
                                    "passing_tds"
                            );


                    int interceptions =
                            getInt(
                                    record,
                                    "passing_interceptions"
                            );


                    int passingFirstDowns =
                            getInt(
                                    record,
                                    "passing_first_downs"
                            );


                    int passingTwoPointConversions =
                            getInt(
                                    record,
                                    "passing_2pt_conversions"
                            );


                    // =================================================
                    // Rushing
                    // =================================================

                    int rushingYards =
                            getInt(
                                    record,
                                    "rushing_yards"
                            );


                    int rushingTouchdowns =
                            getInt(
                                    record,
                                    "rushing_tds"
                            );


                    int rushingFirstDowns =
                            getInt(
                                    record,
                                    "rushing_first_downs"
                            );


                    int rushingTwoPointConversions =
                            getInt(
                                    record,
                                    "rushing_2pt_conversions"
                            );


                    // =================================================
                    // Receiving
                    // =================================================

                    int receptions =
                            getInt(
                                    record,
                                    "receptions"
                            );


                    int receivingYards =
                            getInt(
                                    record,
                                    "receiving_yards"
                            );


                    int receivingTouchdowns =
                            getInt(
                                    record,
                                    "receiving_tds"
                            );


                    int receivingFirstDowns =
                            getInt(
                                    record,
                                    "receiving_first_downs"
                            );


                    int receivingTwoPointConversions =
                            getInt(
                                    record,
                                    "receiving_2pt_conversions"
                            );


                    // =================================================
                    // Fumbles
                    // =================================================

                    int fumblesLost =

                            getInt(
                                    record,
                                    "sack_fumbles_lost"
                            )

                                    +

                                    getInt(
                                            record,
                                            "rushing_fumbles_lost"
                                    )

                                    +

                                    getInt(
                                            record,
                                            "receiving_fumbles_lost"
                                    );


                    // =================================================
                    // Kicker
                    // =================================================

                    int fieldGoalsMade =
                            getInt(
                                    record,
                                    "fg_made"
                            );


                    int fieldGoalsMissed =
                            getInt(
                                    record,
                                    "fg_missed"
                            );


                    int fieldGoalsMade0To19 =
                            getInt(
                                    record,
                                    "fg_made_0_19"
                            );


                    int fieldGoalsMade20To29 =
                            getInt(
                                    record,
                                    "fg_made_20_29"
                            );


                    int fieldGoalsMade30To39 =
                            getInt(
                                    record,
                                    "fg_made_30_39"
                            );


                    int fieldGoalsMade40To49 =
                            getInt(
                                    record,
                                    "fg_made_40_49"
                            );


                    int fieldGoalsMade50To59 =
                            getInt(
                                    record,
                                    "fg_made_50_59"
                            );


                    int fieldGoalsMade60Plus =
                            getInt(
                                    record,
                                    "fg_made_60_"
                            );


                    int fieldGoalsMissed0To19 =
                            getInt(
                                    record,
                                    "fg_missed_0_19"
                            );


                    int fieldGoalsMissed20To29 =
                            getInt(
                                    record,
                                    "fg_missed_20_29"
                            );


                    int fieldGoalsMissed30To39 =
                            getInt(
                                    record,
                                    "fg_missed_30_39"
                            );


                    int fieldGoalsMissed40To49 =
                            getInt(
                                    record,
                                    "fg_missed_40_49"
                            );


                    int fieldGoalsMissed50To59 =
                            getInt(
                                    record,
                                    "fg_missed_50_59"
                            );


                    int fieldGoalsMissed60Plus =
                            getInt(
                                    record,
                                    "fg_missed_60_"
                            );


                    int extraPointsMade =
                            getInt(
                                    record,
                                    "pat_made"
                            );


                    int extraPointsMissed =
                            getInt(
                                    record,
                                    "pat_missed"
                            );


                    // =================================================
                    // 기본 PPR 점수
                    // =================================================

                    double fantasyPoints =
                            fantasyScoreService
                                    .calculateScore(
                                            passingYards,
                                            passingTouchdowns,
                                            interceptions,
                                            rushingYards,
                                            rushingTouchdowns,
                                            receptions,
                                            receivingYards,
                                            receivingTouchdowns,
                                            fumblesLost,
                                            ScoringFormat.PPR
                                    );


                    // =================================================
                    // Create / Update
                    // =================================================

                    String key =
                            createKey(
                                    player.getId(),
                                    week
                            );


                    PlayerStats stats =
                            existingStatsByKey
                                    .get(
                                            key
                                    );


                    if (
                            stats == null
                    ) {

                        stats =
                                new PlayerStats(
                                        player,
                                        season,
                                        week,
                                        fantasyPoints,
                                        passingYards,
                                        passingTouchdowns,
                                        interceptions,
                                        rushingYards,
                                        rushingTouchdowns,
                                        receptions,
                                        receivingYards,
                                        receivingTouchdowns,
                                        fumblesLost
                                );


                        existingStatsByKey.put(
                                key,
                                stats
                        );


                        createdStats++;

                    } else {

                        stats.updateStats(
                                fantasyPoints,
                                passingYards,
                                passingTouchdowns,
                                interceptions,
                                rushingYards,
                                rushingTouchdowns,
                                receptions,
                                receivingYards,
                                receivingTouchdowns,
                                fumblesLost
                        );


                        updatedStats++;
                    }


                    // =================================================
                    // Advanced Stats
                    // =================================================

                    stats.updateAdvancedStats(
                            passingFirstDowns,
                            passingTwoPointConversions,
                            rushingFirstDowns,
                            rushingTwoPointConversions,
                            receivingFirstDowns,
                            receivingTwoPointConversions
                    );


                    // =================================================
                    // Kicker Stats
                    // =================================================

                    stats.updateKickingStats(
                            fieldGoalsMade,
                            fieldGoalsMissed,
                            fieldGoalsMade0To19,
                            fieldGoalsMade20To29,
                            fieldGoalsMade30To39,
                            fieldGoalsMade40To49,
                            fieldGoalsMade50To59,
                            fieldGoalsMade60Plus,
                            fieldGoalsMissed0To19,
                            fieldGoalsMissed20To29,
                            fieldGoalsMissed30To39,
                            fieldGoalsMissed40To49,
                            fieldGoalsMissed50To59,
                            fieldGoalsMissed60Plus,
                            extraPointsMade,
                            extraPointsMissed
                    );


                    statsToSave.put(
                            key,
                            stats
                    );
                }
            }


            // =====================================================
            // 5. PBP Offensive Big Plays
            // =====================================================

            createdStats +=
                    syncOffensiveBigPlays(
                            season,
                            playerByGsis,
                            existingStatsByKey,
                            statsToSave
                    );


            // =====================================================
            // DB 저장
            // =====================================================

            playerStatsRepository
                    .saveAll(
                            statsToSave.values()
                    );


            return new NflverseStatsSyncResult(
                    season,
                    sourceRows,
                    regularSeasonRows,
                    matchedRows,
                    createdStats,
                    updatedStats,
                    skippedNoGsisMatch,
                    skippedUnsupportedPosition
            );


        } catch (
                Exception e
        ) {

            throw new RuntimeException(
                    "nflverse stats synchronization failed",
                    e
            );
        }
    }


    /**
     * nflverse PBP에서 passer/receiver/rusher의 big play를
     * player + season + week 단위로 집계합니다.
     */
    private int syncOffensiveBigPlays(
            int season,
            Map<String, Player> playerByGsis,
            Map<String, PlayerStats> existingStatsByKey,
            Map<String, PlayerStats> statsToSave
    )
            throws Exception {

        String url =
                NFLVERSE_PBP_URL_FORMAT
                        .formatted(
                                season
                        );


        HttpRequest request =
                HttpRequest
                        .newBuilder()

                        .uri(
                                URI.create(
                                        url
                                )
                        )

                        .timeout(
                                Duration.ofMinutes(5)
                        )

                        .GET()

                        .build();


        HttpResponse<InputStream> response =
                httpClient.send(
                        request,
                        HttpResponse
                                .BodyHandlers
                                .ofInputStream()
                );


        if (
                response.statusCode() < 200
                        ||
                        response.statusCode() >= 300
        ) {

            response.body().close();

            throw new RuntimeException(
                    "nflverse PBP request failed. status="
                            + response.statusCode()
                            + ", season="
                            + season
            );
        }


        CSVFormat format =
                CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get();


        Set<String> observedPlayerWeekKeys =
                new HashSet<>();


        Map<String, OffensiveBigPlayAggregate>
                bigPlaysByPlayerWeek =
                new HashMap<>();


        try (
                InputStream inputStream =
                        response.body();

                InputStreamReader reader =
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        );

                CSVParser parser =
                        CSVParser.parse(
                                reader,
                                format
                        )
        ) {

            for (
                    CSVRecord record
                    :
                    parser
            ) {

                String seasonType =
                        getString(
                                record,
                                "season_type"
                        );


                if (
                        !"REG".equalsIgnoreCase(
                                seasonType
                        )
                ) {

                    continue;
                }


                int rowSeason =
                        getInt(
                                record,
                                "season"
                        );


                if (
                        rowSeason != season
                ) {

                    continue;
                }


                int week =
                        getInt(
                                record,
                                "week"
                        );


                if (
                        week <= 0
                ) {

                    continue;
                }


                double passingYards =
                        getDouble(
                                record,
                                "passing_yards"
                        );


                boolean completedPass =
                        getInt(
                                record,
                                "complete_pass"
                        )
                                == 1;


                boolean passingTouchdown =
                        getInt(
                                record,
                                "pass_touchdown"
                        )
                                == 1;


                Player passer = playerByGsis.get(
                        getString(record, "passer_player_id")
                );

                if (passer != null) {
                    String key = createKey(passer.getId(), week);
                    observedPlayerWeekKeys.add(key);

                    if (passingYards >= 40.0 && (completedPass || passingTouchdown)) {
                        OffensiveBigPlayAggregate aggregate = bigPlaysByPlayerWeek
                                .computeIfAbsent(key, ignored -> new OffensiveBigPlayAggregate(passer, week));

                        if (completedPass) aggregate.passCompletions40Plus++;
                        if (passingTouchdown) {
                            aggregate.passingTouchdowns40Plus++;
                            if (passingYards >= 50.0) aggregate.passingTouchdowns50Plus++;
                        }
                    }
                }

                double receivingYards = getDouble(record, "receiving_yards");
                Player receiver = playerByGsis.get(
                        getString(record, "receiver_player_id")
                );

                if (receiver != null) {
                    String key = createKey(receiver.getId(), week);
                    observedPlayerWeekKeys.add(key);

                    if (receivingYards >= 40.0 && (completedPass || passingTouchdown)) {
                        OffensiveBigPlayAggregate aggregate = bigPlaysByPlayerWeek
                                .computeIfAbsent(key, ignored -> new OffensiveBigPlayAggregate(receiver, week));

                        if (completedPass) aggregate.receptions40Plus++;
                        if (passingTouchdown) {
                            aggregate.receivingTouchdowns40Plus++;
                            if (receivingYards >= 50.0) aggregate.receivingTouchdowns50Plus++;
                        }
                    }
                }

                double rushingYards = getDouble(record, "rushing_yards");
                Player rusher = playerByGsis.get(
                        getString(record, "rusher_player_id")
                );

                if (rusher != null) {
                    String key = createKey(rusher.getId(), week);
                    observedPlayerWeekKeys.add(key);

                    if (rushingYards >= 40.0) {
                        OffensiveBigPlayAggregate aggregate = bigPlaysByPlayerWeek
                                .computeIfAbsent(key, ignored -> new OffensiveBigPlayAggregate(rusher, week));

                        aggregate.rushes40Plus++;
                        if (getInt(record, "rush_touchdown") == 1) {
                            aggregate.rushingTouchdowns40Plus++;
                            if (rushingYards >= 50.0) aggregate.rushingTouchdowns50Plus++;
                        }
                    }
                }

                boolean specialTeamsPlay = getInt(record, "special_teams_play") == 1;
                String recovery1 = getString(record, "fumble_recovery_1_player_id");
                String recovery2 = getString(record, "fumble_recovery_2_player_id");

                if (specialTeamsPlay) {
                    Set<String> forcedFumblePlayers = new HashSet<>();
                    for (String gsisId : new String[] {
                            getString(record, "forced_fumble_player_1_player_id"),
                            getString(record, "forced_fumble_player_2_player_id")
                    }) {
                        if (gsisId != null && forcedFumblePlayers.add(gsisId)) {
                            Player player = playerByGsis.get(gsisId);
                            if (player != null) {
                                String key = createKey(player.getId(), week);
                                bigPlaysByPlayerWeek.computeIfAbsent(key,
                                        ignored -> new OffensiveBigPlayAggregate(player, week))
                                        .specialTeamsFumblesForced++;
                            }
                        }
                    }

                    Set<String> recoveryPlayers = new HashSet<>();
                    for (String gsisId : new String[] { recovery1, recovery2 }) {
                        if (gsisId != null && recoveryPlayers.add(gsisId)) {
                            Player player = playerByGsis.get(gsisId);
                            if (player != null) {
                                String key = createKey(player.getId(), week);
                                bigPlaysByPlayerWeek.computeIfAbsent(key,
                                        ignored -> new OffensiveBigPlayAggregate(player, week))
                                        .specialTeamsFumbleRecoveries++;
                            }
                        }
                    }
                }

                if (getInt(record, "touchdown") == 1) {
                    String tdPlayerId = getString(record, "td_player_id");
                    Player tdPlayer = playerByGsis.get(tdPlayerId);
                    if (tdPlayer != null) {
                        boolean fumbleRecoveryTouchdown = tdPlayerId.equals(recovery1)
                                || tdPlayerId.equals(recovery2);
                        if (specialTeamsPlay || fumbleRecoveryTouchdown) {
                            String key = createKey(tdPlayer.getId(), week);
                            OffensiveBigPlayAggregate aggregate = bigPlaysByPlayerWeek
                                    .computeIfAbsent(key,
                                            ignored -> new OffensiveBigPlayAggregate(tdPlayer, week));
                            if (specialTeamsPlay) aggregate.specialTeamsTouchdowns++;
                            if (fumbleRecoveryTouchdown) aggregate.fumbleRecoveryTouchdowns++;
                        }
                    }
                }
            }
        }


        /*
         * 재동기화 시 이전 값이 남지 않도록
         * PBP에서 확인된 player/week를 먼저 0으로 초기화합니다.
         */
        for (
                String key
                :
                observedPlayerWeekKeys
        ) {

            PlayerStats stats =
                    existingStatsByKey
                            .get(
                                    key
                            );


            if (
                    stats == null
            ) {

                continue;
            }


            stats.updateFortyPlusYardPassCompletions(
                    0
            );


            stats.updateFortyPlusYardPassingTouchdowns(
                    0
            );

            stats.updateAdditionalOffensiveBigPlays(0, 0, 0, 0, 0, 0, 0);


            statsToSave.put(
                    key,
                    stats
            );
        }

        // Previously credited players may have no qualifying play in the refreshed PBP.
        for (Map.Entry<String, PlayerStats> entry : existingStatsByKey.entrySet()) {
            PlayerStats stats = entry.getValue();
            if (stats.getSpecialTeamsFumblesForced() != 0
                    || stats.getSpecialTeamsFumbleRecoveries() != 0
                    || stats.getPlayerSpecialTeamsTouchdowns() != 0
                    || stats.getFumbleRecoveryTouchdowns() != 0) {
                stats.updateIndividualSpecialTeamsStats(0, 0, 0, 0);
                statsToSave.put(entry.getKey(), stats);
            }
        }


        int createdStats =
                0;


        for (
                Map.Entry<String, OffensiveBigPlayAggregate> entry
                :
                bigPlaysByPlayerWeek.entrySet()
        ) {

            String key =
                    entry.getKey();


            OffensiveBigPlayAggregate aggregate =
                    entry.getValue();


            PlayerStats stats =
                    existingStatsByKey
                            .get(
                                    key
                            );


            if (
                    stats == null
            ) {

                stats =
                        new PlayerStats(
                                aggregate.player,
                                season,
                                aggregate.week,
                                0.0,
                                0,
                                0,
                                0,
                                0
                        );


                existingStatsByKey.put(
                        key,
                        stats
                );


                createdStats++;
            }


            stats.updateFortyPlusYardPassCompletions(
                    aggregate.passCompletions40Plus
            );


            stats.updateFortyPlusYardPassingTouchdowns(
                    aggregate.passingTouchdowns40Plus
            );

            stats.updateAdditionalOffensiveBigPlays(
                    aggregate.passingTouchdowns50Plus,
                    aggregate.receptions40Plus,
                    aggregate.receivingTouchdowns40Plus,
                    aggregate.receivingTouchdowns50Plus,
                    aggregate.rushes40Plus,
                    aggregate.rushingTouchdowns40Plus,
                    aggregate.rushingTouchdowns50Plus
            );

            stats.updateIndividualSpecialTeamsStats(
                    aggregate.specialTeamsFumblesForced,
                    aggregate.specialTeamsFumbleRecoveries,
                    aggregate.specialTeamsTouchdowns,
                    aggregate.fumbleRecoveryTouchdowns
            );


            statsToSave.put(
                    key,
                    stats
            );
        }


        return createdStats;
    }


    private String createKey(
            Long playerId,
            int week
    ) {

        return playerId
                + ":"
                + week;
    }


    private String getString(
            CSVRecord record,
            String column
    ) {

        if (
                !record.isMapped(column)
        ) {

            return null;
        }


        String value =
                record.get(
                        column
                );


        if (
                value == null
                        ||
                        value.isBlank()
                        ||
                        value.equalsIgnoreCase("NA")
        ) {

            return null;
        }


        return value.trim();
    }


    private int getInt(
            CSVRecord record,
            String column
    ) {

        String value =
                getString(
                        record,
                        column
                );


        if (
                value == null
        ) {

            return 0;
        }


        try {

            return (int) Math.round(
                    Double.parseDouble(
                            value
                    )
            );

        } catch (
                NumberFormatException e
        ) {

            return 0;
        }
    }


    private double getDouble(
            CSVRecord record,
            String column
    ) {

        String value =
                getString(
                        record,
                        column
                );


        if (
                value == null
        ) {

            return 0.0;
        }


        try {

            return Double.parseDouble(
                    value
            );

        } catch (
                NumberFormatException e
        ) {

            return 0.0;
        }
    }


    private static class OffensiveBigPlayAggregate {

        private final Player player;

        private final int week;

        private int passCompletions40Plus;

        private int passingTouchdowns40Plus;

        private int passingTouchdowns50Plus;

        private int receptions40Plus;

        private int receivingTouchdowns40Plus;

        private int receivingTouchdowns50Plus;

        private int rushes40Plus;

        private int rushingTouchdowns40Plus;

        private int rushingTouchdowns50Plus;

        private int specialTeamsFumblesForced;

        private int specialTeamsFumbleRecoveries;

        private int specialTeamsTouchdowns;

        private int fumbleRecoveryTouchdowns;


        private OffensiveBigPlayAggregate(
                Player player,
                int week
        ) {

            this.player =
                    player;

            this.week =
                    week;
        }


    }
}
