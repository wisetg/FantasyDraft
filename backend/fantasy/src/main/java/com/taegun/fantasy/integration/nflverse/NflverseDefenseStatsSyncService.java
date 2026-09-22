package com.taegun.fantasy.integration.nflverse;

import com.taegun.fantasy.player.Player;
import com.taegun.fantasy.player.PlayerRepository;
import com.taegun.fantasy.player.PlayerStats;
import com.taegun.fantasy.player.PlayerStatsRepository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.stereotype.Service;

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
import java.util.Optional;
import java.util.Set;


/**
 * nflverse Team Stats + Play-by-Play를 이용해
 * Fantasy Team Defense 기록을 동기화합니다.
 *
 * 처리 항목:
 *
 * Sack
 * INT
 * Forced Fumble
 * Fumble Recovery
 * Safety
 * Blocked Kick
 * Defensive TD
 * Special Teams TD
 * Sleeper 방식 Points Allowed
 */
@Service
public class NflverseDefenseStatsSyncService {

    /**
     * Team Weekly Stats
     */
    private static final String
            NFLVERSE_TEAM_URL =

            "https://github.com/"
                    + "nflverse/nflverse-data/"
                    + "releases/download/"
                    + "stats_team/"
                    + "stats_team_week_%d.csv";


    /**
     * Play-by-Play
     */
    private static final String
            NFLVERSE_PBP_URL =

            "https://github.com/"
                    + "nflverse/nflverse-data/"
                    + "releases/download/"
                    + "pbp/"
                    + "play_by_play_%d.csv";


    private final PlayerRepository
            playerRepository;

    private final PlayerStatsRepository
            playerStatsRepository;

    private final HttpClient
            httpClient;


    public NflverseDefenseStatsSyncService(
            PlayerRepository playerRepository,
            PlayerStatsRepository playerStatsRepository
    ) {

        this.playerRepository =
                playerRepository;

        this.playerStatsRepository =
                playerStatsRepository;


        this.httpClient =
                HttpClient
                        .newBuilder()

                        .connectTimeout(
                                Duration.ofSeconds(20)
                        )

                        .followRedirects(
                                HttpClient.Redirect.NORMAL
                        )

                        .build();
    }


    /**
     * 지정 시즌 Team Defense 전체 동기화
     */
    public NflverseDefenseSyncResult syncSeason(
            int season
    ) {

        try {

            // =====================================================
            // 기존 PlayerStats
            // =====================================================

            List<PlayerStats> existingStats =
                    playerStatsRepository
                            .findBySeason(
                                    season
                            );


            /**
             * Key:
             *
             * playerId:week
             */
            Map<String, PlayerStats>
                    statsByKey =
                    new HashMap<>();


            for (
                    PlayerStats stats
                    :
                    existingStats
            ) {

                statsByKey.put(
                        createPlayerWeekKey(
                                stats
                                        .getPlayer()
                                        .getId(),
                                stats.getWeek()
                        ),
                        stats
                );
            }


            /**
             * 실제 저장할 Stats
             *
             * 동일 Player/Week이 여러 번 들어가도
             * 중복 save를 막습니다.
             */
            Map<String, PlayerStats>
                    statsToSave =
                    new LinkedHashMap<>();


            // =====================================================
            // 1. Team Stats
            // =====================================================

            TeamStatsSyncCounters teamCounters =
                    syncTeamStats(
                            season,
                            statsByKey,
                            statsToSave
                    );


            // =====================================================
            // 2. Sleeper 방식 Points Allowed
            // =====================================================

            PointsAllowedResult
                    pointsAllowedResult =
                    calculatePointsAllowed(
                            season
                    );


            int pointsAllowedUpdated =
                    0;


            int skippedNoDefensePlayer =
                    teamCounters
                            .skippedNoDefensePlayer;


            /*
             * team:week → points allowed
             */
            for (
                    Map.Entry<String, Integer> entry
                    :
                    pointsAllowedResult
                            .pointsAllowedByTeamWeek
                            .entrySet()
            ) {

                String teamWeekKey =
                        entry.getKey();


                String[] parts =
                        teamWeekKey
                                .split(
                                        ":",
                                        2
                                );


                if (
                        parts.length != 2
                ) {

                    continue;
                }


                String team =
                        parts[0];


                int week;


                try {

                    week =
                            Integer.parseInt(
                                    parts[1]
                            );

                } catch (
                        NumberFormatException e
                ) {

                    continue;
                }


                Optional<Player> defensePlayer =
                        findDefensePlayer(
                                team
                        );


                if (
                        defensePlayer.isEmpty()
                ) {

                    skippedNoDefensePlayer++;

                    continue;
                }


                Player player =
                        defensePlayer.get();


                String playerWeekKey =
                        createPlayerWeekKey(
                                player.getId(),
                                week
                        );


                PlayerStats stats =
                        statsByKey
                                .get(
                                        playerWeekKey
                                );


                /*
                 * Team Stats 쪽에서 Row가 없었던
                 * 특수 상황에도 PA는 저장되도록
                 * PlayerStats를 만들어줍니다.
                 */
                if (
                        stats == null
                ) {

                    stats =
                            new PlayerStats(
                                    player,
                                    season,
                                    week,
                                    0.0,
                                    0,
                                    0,
                                    0,
                                    0
                            );


                    statsByKey.put(
                            playerWeekKey,
                            stats
                    );


                    teamCounters
                            .createdStats++;
                }


                stats.updateDefensePointsAllowed(
                        entry.getValue()
                );

                stats.updateDefenseSpecialTeamsFumbles(
                        pointsAllowedResult.specialTeamsFumblesForcedByTeamWeek
                                .getOrDefault(teamWeekKey, 0),
                        pointsAllowedResult.specialTeamsFumbleRecoveriesByTeamWeek
                                .getOrDefault(teamWeekKey, 0)
                );


                statsToSave.put(
                        playerWeekKey,
                        stats
                );


                pointsAllowedUpdated++;
            }


            // =====================================================
            // 3. DB 저장
            // =====================================================

            playerStatsRepository
                    .saveAll(
                            statsToSave
                                    .values()
                    );


            // =====================================================
            // 결과
            // =====================================================

            return new NflverseDefenseSyncResult(
                    season,
                    teamCounters.sourceRows,
                    pointsAllowedResult.pbpRows,
                    teamCounters.matchedDefenses,
                    teamCounters.createdStats,
                    teamCounters.updatedStats,
                    pointsAllowedUpdated,
                    skippedNoDefensePlayer
            );


        } catch (
                Exception e
        ) {

            throw new RuntimeException(
                    "nflverse defense synchronization failed",
                    e
            );
        }
    }


    // =============================================================
    // Team Stats
    // =============================================================

    private TeamStatsSyncCounters syncTeamStats(
            int season,
            Map<String, PlayerStats> statsByKey,
            Map<String, PlayerStats> statsToSave
    )
            throws Exception {

        String url =
                NFLVERSE_TEAM_URL
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
                                Duration.ofSeconds(120)
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
                    "nflverse team stats request failed. status="
                            + response.statusCode()
            );
        }


        CSVFormat format =
                createCsvFormat();


        TeamStatsSyncCounters counters =
                new TeamStatsSyncCounters();


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

                counters.sourceRows++;


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


                String team =
                        normalizeTeamCode(
                                getString(
                                        record,
                                        "team"
                                )
                        );


                if (
                        team == null
                ) {

                    continue;
                }


                Optional<Player> defensePlayer =
                        findDefensePlayer(
                                team
                        );


                if (
                        defensePlayer.isEmpty()
                ) {

                    counters
                            .skippedNoDefensePlayer++;

                    continue;
                }


                Player player =
                        defensePlayer.get();


                counters.matchedDefenses++;


                // =================================================
                // Defense Stats
                // =================================================

                double sacks =
                        getDouble(
                                record,
                                "def_sacks"
                        );


                int interceptions =
                        getInt(
                                record,
                                "def_interceptions"
                        );


                int forcedFumbles =
                        getInt(
                                record,
                                "def_fumbles_forced"
                        );


                int fumbleRecoveries =
                        getInt(
                                record,
                                "fumble_recovery_opp"
                        );


                int defensiveTouchdowns =
                        getInt(
                                record,
                                "def_tds"
                        );


                int safeties =
                        getInt(
                                record,
                                "def_safeties"
                        );


                int blockedKicks =

                        getInt(
                                record,
                                "def_punt_blocks"
                        )

                                +

                                getInt(
                                        record,
                                        "def_pat_blocks"
                                )

                                +

                                getInt(
                                        record,
                                        "def_fg_blocks"
                                );


                int specialTeamsTouchdowns =
                        getInt(
                                record,
                                "special_teams_tds"
                        );


                String key =
                        createPlayerWeekKey(
                                player.getId(),
                                week
                        );


                PlayerStats stats =
                        statsByKey
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
                                    0.0,
                                    0,
                                    0,
                                    0,
                                    0
                            );


                    statsByKey.put(
                            key,
                            stats
                    );


                    counters.createdStats++;

                } else {

                    counters.updatedStats++;
                }


                stats.updateDefenseStats(
                        sacks,
                        interceptions,
                        forcedFumbles,
                        fumbleRecoveries,
                        defensiveTouchdowns,
                        safeties,
                        blockedKicks,
                        specialTeamsTouchdowns
                );


                statsToSave.put(
                        key,
                        stats
                );
            }
        }


        return counters;
    }


    // =============================================================
    // Sleeper Points Allowed
    // =============================================================

    /**
     * Sleeper Points Allowed 규칙:
     *
     * 포함:
     *
     * 상대 FG
     * 상대 PAT
     * 상대 2PT
     * 공격 TD
     * Kick/Punt Return TD
     * Blocked Punt Return TD 등
     *
     * 제외:
     *
     * 상대 수비가 만든 Defensive TD 자체의 6점
     *
     * 하지만 그 Defensive TD 이후 PAT / 2PT는 다시 포함됩니다.
     */
    private PointsAllowedResult calculatePointsAllowed(
            int season
    )
            throws Exception {

        String url =
                NFLVERSE_PBP_URL
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

                        /*
                         * PBP 파일은 크기 때문에
                         * Team Stats보다 Timeout을 길게 잡습니다.
                         */
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

            throw new RuntimeException(
                    "nflverse PBP request failed. status="
                            + response.statusCode()
            );
        }


        Map<String, Integer>
                pointsAllowedByTeamWeek =
                new HashMap<>();

        Map<String, Integer> specialTeamsFumblesForcedByTeamWeek = new HashMap<>();
        Map<String, Integer> specialTeamsFumbleRecoveriesByTeamWeek = new HashMap<>();


        int pbpRows =
                0;


        CSVFormat format =
                createCsvFormat();


        /*
         * PBP 전체 파일을 String으로 메모리에 올리지 않고
         * InputStream으로 한 줄씩 읽습니다.
         */
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

                pbpRows++;


                // =================================================
                // Regular Season
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


                String homeTeam =
                        normalizeTeamCode(
                                getString(
                                        record,
                                        "home_team"
                                )
                        );


                String awayTeam =
                        normalizeTeamCode(
                                getString(
                                        record,
                                        "away_team"
                                )
                        );


                if (
                        homeTeam == null
                                ||
                                awayTeam == null
                ) {

                    continue;
                }


                /*
                 * 점수가 0인 경기/팀도 반드시
                 * Map에 존재하게 만듭니다.
                 *
                 * 그래야 실제 Shutout을 0으로 저장할 수 있습니다.
                 */
                initializePointsAllowed(
                        pointsAllowedByTeamWeek,
                        homeTeam,
                        week
                );


                initializePointsAllowed(
                        pointsAllowedByTeamWeek,
                        awayTeam,
                        week
                );

                if (getInt(record, "special_teams_play") == 1) {
                    Set<String> forcedFumblePlayers = new HashSet<>();
                    countSpecialTeamsFumble(record, "forced_fumble_player_1_player_id",
                            "forced_fumble_player_1_team", week, forcedFumblePlayers,
                            specialTeamsFumblesForcedByTeamWeek);
                    countSpecialTeamsFumble(record, "forced_fumble_player_2_player_id",
                            "forced_fumble_player_2_team", week, forcedFumblePlayers,
                            specialTeamsFumblesForcedByTeamWeek);

                    Set<String> recoveryPlayers = new HashSet<>();
                    countSpecialTeamsFumble(record, "fumble_recovery_1_player_id",
                            "fumble_recovery_1_team", week, recoveryPlayers,
                            specialTeamsFumbleRecoveriesByTeamWeek);
                    countSpecialTeamsFumble(record, "fumble_recovery_2_player_id",
                            "fumble_recovery_2_team", week, recoveryPlayers,
                            specialTeamsFumbleRecoveriesByTeamWeek);
                }


                // =================================================
                // Touchdown
                // =================================================

                if (
                        getInt(
                                record,
                                "touchdown"
                        )
                                == 1
                ) {

                    String touchdownTeam =
                            normalizeTeamCode(
                                    getString(
                                            record,
                                            "td_team"
                                    )
                            );


                    if (
                            touchdownTeam != null
                    ) {

                        boolean defensiveTouchdown =
                                isDefensiveTouchdown(
                                        record,
                                        touchdownTeam
                                );


                        /*
                         * Defensive TD 자체의 6점은
                         * Sleeper Points Allowed에서 제외합니다.
                         */
                        if (
                                !defensiveTouchdown
                        ) {

                            addPointsToOpponentDefense(
                                    pointsAllowedByTeamWeek,
                                    touchdownTeam,
                                    homeTeam,
                                    awayTeam,
                                    week,
                                    6
                            );
                        }
                    }
                }


                // =================================================
                // Field Goal
                // =================================================

                String fieldGoalResult =
                        getString(
                                record,
                                "field_goal_result"
                        );


                if (
                        "made".equalsIgnoreCase(
                                fieldGoalResult
                        )
                ) {

                    String scoringTeam =
                            normalizeTeamCode(
                                    getString(
                                            record,
                                            "posteam"
                                    )
                            );


                    addPointsToOpponentDefense(
                            pointsAllowedByTeamWeek,
                            scoringTeam,
                            homeTeam,
                            awayTeam,
                            week,
                            3
                    );
                }


                // =================================================
                // Extra Point
                // =================================================

                String extraPointResult =
                        getString(
                                record,
                                "extra_point_result"
                        );


                if (
                        "good".equalsIgnoreCase(
                                extraPointResult
                        )
                ) {

                    String scoringTeam =
                            normalizeTeamCode(
                                    getString(
                                            record,
                                            "posteam"
                                    )
                            );


                    addPointsToOpponentDefense(
                            pointsAllowedByTeamWeek,
                            scoringTeam,
                            homeTeam,
                            awayTeam,
                            week,
                            1
                    );
                }


                // =================================================
                // 2-Point Conversion
                // =================================================

                String twoPointResult =
                        getString(
                                record,
                                "two_point_conv_result"
                        );


                if (
                        "success".equalsIgnoreCase(
                                twoPointResult
                        )
                ) {

                    String scoringTeam =
                            normalizeTeamCode(
                                    getString(
                                            record,
                                            "posteam"
                                    )
                            );


                    addPointsToOpponentDefense(
                            pointsAllowedByTeamWeek,
                            scoringTeam,
                            homeTeam,
                            awayTeam,
                            week,
                            2
                    );
                }
            }
        }


        return new PointsAllowedResult(
                pbpRows,
                pointsAllowedByTeamWeek,
                specialTeamsFumblesForcedByTeamWeek,
                specialTeamsFumbleRecoveriesByTeamWeek
        );
    }

    private void countSpecialTeamsFumble(
            CSVRecord record,
            String playerIdColumn,
            String teamColumn,
            int week,
            Set<String> seenPlayerIds,
            Map<String, Integer> countsByTeamWeek
    ) {
        String playerId = getString(record, playerIdColumn);
        String team = normalizeTeamCode(getString(record, teamColumn));
        if (playerId != null && team != null && seenPlayerIds.add(playerId)) {
            countsByTeamWeek.merge(createTeamWeekKey(team, week), 1, Integer::sum);
        }
    }


    /**
     * TD가 상대 수비가 만든 TD인지 판별합니다.
     *
     * 일반 공격 TD:
     *
     * td_team == posteam
     *
     * Pick Six / Defensive Fumble Return TD:
     *
     * td_team == defteam
     * td_team != posteam
     *
     * Punt / Kick / Blocked Punt Return TD:
     *
     * special_teams_play == 1
     *
     * Sleeper에서는 Special Teams Return TD는
     * Points Allowed에 포함되므로 defensive TD로
     * 취급하지 않습니다.
     */
    private boolean isDefensiveTouchdown(
            CSVRecord record,
            String touchdownTeam
    ) {

        int specialTeamsPlay =
                getInt(
                        record,
                        "special_teams_play"
                );


        /*
         * Special Teams TD는 Points Allowed 대상
         */
        if (
                specialTeamsPlay == 1
        ) {

            return false;
        }


        String possessionTeam =
                normalizeTeamCode(
                        getString(
                                record,
                                "posteam"
                        )
                );


        String defenseTeam =
                normalizeTeamCode(
                        getString(
                                record,
                                "defteam"
                        )
                );


        /*
         * 가장 명확한 Defensive TD 패턴
         */
        if (
                defenseTeam != null
                        &&
                        touchdownTeam.equals(
                                defenseTeam
                        )
                        &&
                        (
                                possessionTeam == null
                                        ||
                                        !touchdownTeam.equals(
                                                possessionTeam
                                        )
                        )
        ) {

            return true;
        }


        /*
         * fallback
         */
        return possessionTeam != null
                &&
                !touchdownTeam.equals(
                        possessionTeam
                );
    }


    /**
     * 득점 팀의 상대 D/ST에 Points Allowed를 더합니다.
     */
    private void addPointsToOpponentDefense(
            Map<String, Integer> pointsAllowed,
            String scoringTeam,
            String homeTeam,
            String awayTeam,
            int week,
            int points
    ) {

        if (
                scoringTeam == null
                        ||
                        homeTeam == null
                        ||
                        awayTeam == null
        ) {

            return;
        }


        String chargedDefense;


        if (
                scoringTeam.equals(
                        homeTeam
                )
        ) {

            chargedDefense =
                    awayTeam;

        } else if (
                scoringTeam.equals(
                        awayTeam
                )
        ) {

            chargedDefense =
                    homeTeam;

        } else {

            return;
        }


        String key =
                createTeamWeekKey(
                        chargedDefense,
                        week
                );


        pointsAllowed.merge(
                key,
                points,
                Integer::sum
        );
    }


    private void initializePointsAllowed(
            Map<String, Integer> pointsAllowed,
            String team,
            int week
    ) {

        String key =
                createTeamWeekKey(
                        team,
                        week
                );


        pointsAllowed.putIfAbsent(
                key,
                0
        );
    }


    // =============================================================
    // Player Lookup
    // =============================================================

    private Optional<Player> findDefensePlayer(
            String team
    ) {

        Optional<Player> defense =
                playerRepository
                        .findFirstByPositionIgnoreCaseAndTeamIgnoreCase(
                                "DEF",
                                team
                        );


        if (
                defense.isPresent()
        ) {

            return defense;
        }


        /*
         * 혹시 기존 데이터가 DST로 들어간 경우도 지원
         */
        return playerRepository
                .findFirstByPositionIgnoreCaseAndTeamIgnoreCase(
                        "DST",
                        team
                );
    }


    // =============================================================
    // Team Code Normalization
    // =============================================================

    private String normalizeTeamCode(
            String team
    ) {

        if (
                team == null
                        ||
                        team.isBlank()
        ) {

            return null;
        }


        String normalized =
                team
                        .trim()
                        .toUpperCase();


        return switch (
                normalized
                ) {

            /*
             * Rams
             */
            case "LA", "STL" ->
                    "LAR";


            /*
             * Jaguars
             */
            case "JAC" ->
                    "JAX";


            /*
             * Washington
             */
            case "WSH" ->
                    "WAS";


            /*
             * Chargers
             */
            case "SD" ->
                    "LAC";


            /*
             * Raiders
             */
            case "OAK" ->
                    "LV";


            default ->
                    normalized;
        };
    }


    // =============================================================
    // Key
    // =============================================================

    private String createPlayerWeekKey(
            Long playerId,
            int week
    ) {

        return playerId
                + ":"
                + week;
    }


    private String createTeamWeekKey(
            String team,
            int week
    ) {

        return team
                + ":"
                + week;
    }


    // =============================================================
    // CSV
    // =============================================================

    private CSVFormat createCsvFormat() {

        return CSVFormat.DEFAULT
                .builder()

                .setHeader()

                .setSkipHeaderRecord(
                        true
                )

                .get();
    }


    private String getString(
            CSVRecord record,
            String column
    ) {

        if (
                !record.isMapped(
                        column
                )
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
                        value.equalsIgnoreCase(
                                "NA"
                        )
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


    // =============================================================
    // Internal Result Classes
    // =============================================================

    private static class TeamStatsSyncCounters {

        private int sourceRows;

        private int matchedDefenses;

        private int createdStats;

        private int updatedStats;

        private int skippedNoDefensePlayer;
    }


    private static class PointsAllowedResult {

        private final int pbpRows;

        private final Map<String, Integer>
                pointsAllowedByTeamWeek;

        private final Map<String, Integer> specialTeamsFumblesForcedByTeamWeek;

        private final Map<String, Integer> specialTeamsFumbleRecoveriesByTeamWeek;


        private PointsAllowedResult(
                int pbpRows,
                Map<String, Integer> pointsAllowedByTeamWeek,
                Map<String, Integer> specialTeamsFumblesForcedByTeamWeek,
                Map<String, Integer> specialTeamsFumbleRecoveriesByTeamWeek
        ) {

            this.pbpRows =
                    pbpRows;

            this.pointsAllowedByTeamWeek =
                    pointsAllowedByTeamWeek;

            this.specialTeamsFumblesForcedByTeamWeek = specialTeamsFumblesForcedByTeamWeek;
            this.specialTeamsFumbleRecoveriesByTeamWeek = specialTeamsFumbleRecoveriesByTeamWeek;
        }
    }
}
