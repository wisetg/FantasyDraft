package com.taegun.fantasy.service;

import com.taegun.fantasy.league.FantasyLeague;
import com.taegun.fantasy.league.FantasyLeagueRepository;
import com.taegun.fantasy.league.FantasyRosterPlayer;
import com.taegun.fantasy.league.FantasyRosterPlayerRepository;

import com.taegun.fantasy.player.Player;
import com.taegun.fantasy.player.PlayerRepository;
import com.taegun.fantasy.player.PlayerStats;
import com.taegun.fantasy.player.PlayerStatsRepository;
import com.taegun.fantasy.player.PlayerValue;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 실제 Fantasy League 환경을 반영해
 * Player Trade Value를 계산합니다.
 *
 * Trade Value V2 핵심:
 *
 * 1. 실제 League Scoring Rule
 * 2. Season Average
 * 3. Recent Average
 * 4. Ceiling
 * 5. Consistency
 * 6. Trend
 * 7. Position Replacement Level
 *
 * Replacement Level은
 *
 * "현재 해당 Fantasy League에서
 * 로스터되지 않은 같은 Position 선수"
 *
 * 를 기준으로 계산합니다.
 */
@Service
public class LeaguePlayerValueService {

    /**
     * Replacement 후보가 되려면
     * 최소한 이 경기 수 이상 기록이 있어야 합니다.
     *
     * 한 경기만 우연히 크게 터진 선수가
     * Replacement Level을 왜곡하지 않도록 합니다.
     */
    private static final int
            MIN_REPLACEMENT_GAMES =
            2;


    private final FantasyLeagueRepository
            fantasyLeagueRepository;

    private final PlayerRepository
            playerRepository;

    private final PlayerStatsRepository
            playerStatsRepository;

    private final FantasyRosterPlayerRepository
            fantasyRosterPlayerRepository;

    private final LeagueScoringService
            leagueScoringService;


    public LeaguePlayerValueService(
            FantasyLeagueRepository fantasyLeagueRepository,
            PlayerRepository playerRepository,
            PlayerStatsRepository playerStatsRepository,
            FantasyRosterPlayerRepository fantasyRosterPlayerRepository,
            LeagueScoringService leagueScoringService
    ) {

        this.fantasyLeagueRepository =
                fantasyLeagueRepository;

        this.playerRepository =
                playerRepository;

        this.playerStatsRepository =
                playerStatsRepository;

        this.fantasyRosterPlayerRepository =
                fantasyRosterPlayerRepository;

        this.leagueScoringService =
                leagueScoringService;
    }


    /**
     * 특정 League 기준 Player Trade Value
     */
    @Transactional(readOnly = true)
    public PlayerValue calculatePlayerValue(
            Long playerId,
            Long fantasyLeagueId
    ) {

        // =====================================================
        // 1. League
        // =====================================================

        FantasyLeague league =
                fantasyLeagueRepository
                        .findById(
                                fantasyLeagueId
                        )
                        .orElseThrow(() ->

                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Fantasy League not found"
                                )
                        );


        // =====================================================
        // 2. Player
        // =====================================================

        Player player =
                playerRepository
                        .findById(
                                playerId
                        )
                        .orElseThrow(() ->

                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Player not found"
                                )
                        );


        // =====================================================
        // 3. League 시즌 전체 Stats
        // =====================================================

        List<PlayerStats> seasonStats =
                playerStatsRepository
                        .findBySeason(
                                league.getSeason()
                        );


        /*
         * playerId → 해당 시즌 Stats
         */
        Map<Long, List<PlayerStats>>
                statsByPlayerId =
                new HashMap<>();


        for (
                PlayerStats stats
                :
                seasonStats
        ) {

            statsByPlayerId
                    .computeIfAbsent(
                            stats
                                    .getPlayer()
                                    .getId(),
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            stats
                    );
        }


        List<PlayerStats> targetStats =
                statsByPlayerId
                        .get(
                                playerId
                        );


        if (
                targetStats == null
                        ||
                        targetStats.isEmpty()
        ) {
            return new PlayerValue(
                    player.getId(),
                    player.getName(),
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "NO_DATA",
                    player.getPosition(),
                    0,
                    0.0,
                    0.0,
                    0.0
            );
        }


        targetStats.sort(
                Comparator
                        .comparingInt(
                                PlayerStats::getWeek
                        )
        );


        // =====================================================
        // 4. Target Player 경기 점수
        // =====================================================

        List<Double> targetScores =
                calculateScores(
                        league,
                        player,
                        targetStats
                );


        double seasonAverage =
                average(
                        targetScores
                );


        double recentAverage =
                calculateRecentAverage(
                        targetScores
                );


        double highestScore =
                targetScores
                        .stream()

                        .mapToDouble(
                                Double::doubleValue
                        )

                        .max()

                        .orElse(0.0);


        double standardDeviation =
                calculateStandardDeviation(
                        targetScores,
                        seasonAverage
                );


        // =====================================================
        // 5. Trend
        // =====================================================

        String trend;


        if (
                recentAverage
                        >
                        seasonAverage + 1.0
        ) {

            trend =
                    "UP";

        } else if (
                recentAverage
                        <
                        seasonAverage - 1.0
        ) {

            trend =
                    "DOWN";

        } else {

            trend =
                    "STABLE";
        }


        double trendScore =
                switch (
                        trend
                        ) {

                    case "UP" ->
                            3.0;

                    case "STABLE" ->
                            1.5;

                    case "DOWN" ->
                            0.0;

                    default ->
                            1.0;
                };


        // =====================================================
        // 6. Consistency
        // =====================================================

        /*
         * 표준편차가 작을수록
         * Consistency가 높습니다.
         *
         * 최대 10점
         */
        double consistencyScore =
                Math.max(
                        0.0,

                        10.0
                                -
                                standardDeviation
                );


        // =====================================================
        // 7. 현재 League에 Roster된 Player ID
        // =====================================================

        List<FantasyRosterPlayer>
                rosterPlayers =
                fantasyRosterPlayerRepository
                        .findByFantasyTeam_FantasyLeague_Id(
                                fantasyLeagueId
                        );


        Set<Long> rosteredPlayerIds =
                new HashSet<>();


        for (
                FantasyRosterPlayer rosterPlayer
                :
                rosterPlayers
        ) {

            rosteredPlayerIds.add(
                    rosterPlayer
                            .getPlayer()
                            .getId()
            );
        }


        // =====================================================
        // 8. Replacement Level
        // =====================================================

        double replacementAverage =
                calculateReplacementAverage(
                        league,
                        player,
                        statsByPlayerId,
                        rosteredPlayerIds
                );


        /*
         * VOR
         *
         * Value Above Replacement
         */
        double valueAboveReplacement =
                Math.max(
                        0.0,

                        seasonAverage
                                -
                                replacementAverage
                );


        double recentValueAboveReplacement =
                Math.max(
                        0.0,

                        recentAverage
                                -
                                replacementAverage
                );


        double ceilingAboveReplacement =
                Math.max(
                        0.0,

                        highestScore
                                -
                                replacementAverage
                );


        // =====================================================
        // 9. Trade Value V2
        // =====================================================

        /*
         * V2 Formula
         *
         * Season VOR       50%
         * Recent VOR       25%
         * Ceiling VOR      10%
         * Season Average   10%
         * Consistency       5%
         *
         * + Trend Bonus
         *
         * 핵심은 절대 Fantasy Point가 아니라
         * 같은 Position의 Replacement Player보다
         * 얼마나 우수한지입니다.
         */
        double valueScore =

                valueAboveReplacement
                        * 0.50

                        +

                        recentValueAboveReplacement
                                * 0.25

                        +

                        ceilingAboveReplacement
                                * 0.10

                        +

                        seasonAverage
                                * 0.10

                        +

                        consistencyScore
                                * 0.05

                        +

                        trendScore;


        return new PlayerValue(
                player.getId(),
                player.getName(),

                round(
                        valueScore
                ),

                round(
                        seasonAverage
                ),

                round(
                        recentAverage
                ),

                round(
                        consistencyScore
                ),

                round(
                        trendScore
                ),

                trend,

                player.getPosition(),

                targetScores.size(),

                round(
                        replacementAverage
                ),

                round(
                        valueAboveReplacement
                ),

                round(
                        recentValueAboveReplacement
                )
        );
    }


    /**
     * =========================================================
     * Replacement Level
     * =========================================================
     *
     * 현재 League에서 로스터되지 않은
     * 같은 Position의 선수들을 찾습니다.
     *
     * 그중 상위 몇 명의 평균을
     * Replacement Level로 사용합니다.
     */
    private double calculateReplacementAverage(
            FantasyLeague league,
            Player targetPlayer,
            Map<Long, List<PlayerStats>> statsByPlayerId,
            Set<Long> rosteredPlayerIds
    ) {

        String targetPosition =
                normalizePosition(
                        targetPlayer
                                .getPosition()
                );


        if (
                targetPosition == null
        ) {

            return 0.0;
        }


        List<Double> replacementCandidates =
                new ArrayList<>();


        /*
         * 시즌 기록이 존재하는 모든 Player 검사
         */
        for (
                Map.Entry<Long, List<PlayerStats>> entry
                :
                statsByPlayerId.entrySet()
        ) {

            Long candidatePlayerId =
                    entry.getKey();


            /*
             * 이미 Fantasy League에서
             * 누군가 보유 중이라면
             * Replacement Player가 아닙니다.
             */
            if (
                    rosteredPlayerIds
                            .contains(
                                    candidatePlayerId
                            )
            ) {

                continue;
            }


            Player candidate =
                    playerRepository
                            .findById(
                                    candidatePlayerId
                            )
                            .orElse(
                                    null
                            );


            if (
                    candidate == null
            ) {

                continue;
            }


            /*
             * 같은 Position만 비교
             */
            String candidatePosition =
                    normalizePosition(
                            candidate
                                    .getPosition()
                    );


            if (
                    candidatePosition == null
                            ||
                            !targetPosition.equals(
                                    candidatePosition
                            )
            ) {

                continue;
            }


            /*
             * 현재 NFL 팀이 없는 FA는
             * Replacement 후보에서 제외
             */
            if (
                    candidate.getTeam() == null
                            ||
                            candidate
                                    .getTeam()
                                    .isBlank()
                            ||
                            candidate
                                    .getTeam()
                                    .equalsIgnoreCase(
                                            "FA"
                                    )
            ) {

                continue;
            }


            List<PlayerStats> candidateStats =
                    entry.getValue();


            if (
                    candidateStats == null
                            ||
                            candidateStats.size()
                                    <
                                    MIN_REPLACEMENT_GAMES
            ) {

                continue;
            }


            List<Double> scores =
                    calculateScores(
                            league,
                            candidate,
                            candidateStats
                    );


            if (
                    scores.size()
                            <
                            MIN_REPLACEMENT_GAMES
            ) {

                continue;
            }


            replacementCandidates.add(
                    average(
                            scores
                    )
            );
        }


        /*
         * 좋은 Free Agent부터 정렬
         */
        replacementCandidates.sort(
                Comparator.reverseOrder()
        );


        if (
                replacementCandidates.isEmpty()
        ) {

            return 0.0;
        }


        /*
         * League 크기에 따라
         * Replacement Pool 크기를 조절합니다.
         *
         * 4~7 팀  → Top 1
         * 8~11 팀 → Top 2
         * 12+ 팀  → Top 3
         *
         * 한 선수의 한두 경기 Fluke 때문에
         * Replacement 기준이 크게 흔들리는 것을 줄입니다.
         */
        int replacementPoolSize =
                Math.max(
                        1,

                        Math.min(
                                3,

                                Math.max(
                                        1,

                                        league.getTotalTeams()
                                                /
                                                4
                                )
                        )
                );


        int actualPoolSize =
                Math.min(
                        replacementPoolSize,

                        replacementCandidates
                                .size()
                );


        double sum =
                0.0;


        for (
                int i = 0;
                i < actualPoolSize;
                i++
        ) {

            sum +=
                    replacementCandidates
                            .get(i);
        }


        return sum
                /
                actualPoolSize;
    }


    /**
     * 실제 League scoring_settings를 이용해서
     * 경기별 Fantasy Point 계산
     */
    private List<Double> calculateScores(
            FantasyLeague league,
            Player player,
            List<PlayerStats> stats
    ) {

        List<PlayerStats> sortedStats =
                new ArrayList<>(
                        stats
                );


        sortedStats.sort(
                Comparator
                        .comparingInt(
                                PlayerStats::getWeek
                        )
        );


        List<Double> scores =
                new ArrayList<>();


        for (
                PlayerStats game
                :
                sortedStats
        ) {

            double score =
                    leagueScoringService
                            .calculateScore(
                                    league,
                                    player,
                                    game
                            );


            scores.add(
                    score
            );
        }


        return scores;
    }


    /**
     * 최근 3경기 평균
     */
    private double calculateRecentAverage(
            List<Double> scores
    ) {

        if (
                scores.isEmpty()
        ) {

            return 0.0;
        }


        int startIndex =
                Math.max(
                        0,

                        scores.size()
                                -
                                3
                );


        List<Double> recent =
                scores.subList(
                        startIndex,
                        scores.size()
                );


        return average(
                recent
        );
    }


    /**
     * 평균
     */
    private double average(
            List<Double> values
    ) {

        if (
                values == null
                        ||
                        values.isEmpty()
        ) {

            return 0.0;
        }


        return values
                .stream()

                .mapToDouble(
                        Double::doubleValue
                )

                .average()

                .orElse(
                        0.0
                );
    }


    /**
     * 표준편차
     */
    private double calculateStandardDeviation(
            List<Double> values,
            double average
    ) {

        if (
                values == null
                        ||
                        values.isEmpty()
        ) {

            return 0.0;
        }


        double variance =
                0.0;


        for (
                double value
                :
                values
        ) {

            double difference =
                    value
                            -
                            average;


            variance +=
                    difference
                            *
                            difference;
        }


        variance =
                variance
                        /
                        values.size();


        return Math.sqrt(
                variance
        );
    }


    /**
     * Position 이름 정규화
     */
    private String normalizePosition(
            String position
    ) {

        if (
                position == null
                        ||
                        position.isBlank()
        ) {

            return null;
        }


        String normalized =
                position
                        .trim()
                        .toUpperCase();


        /*
         * DEF / DST는 같은 Position으로 취급
         */
        if (
                normalized.equals(
                        "DST"
                )
        ) {

            return "DEF";
        }


        return normalized;
    }


    private double round(
            double value
    ) {

        return Math.round(
                value
                        *
                        100.0
        )
                /
                100.0;
    }
}
