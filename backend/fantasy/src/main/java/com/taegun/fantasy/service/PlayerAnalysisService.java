package com.taegun.fantasy.service;

import com.taegun.fantasy.player.Player;
import com.taegun.fantasy.player.PlayerAnalysis;
import com.taegun.fantasy.player.PlayerRepository;
import com.taegun.fantasy.player.PlayerStats;
import com.taegun.fantasy.player.PlayerStatsRepository;
import com.taegun.fantasy.player.ScoringFormat;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;


/**
 * 선수의 경기 데이터를 분석하는 Service입니다.
 *
 * 시즌과 Scoring Format 기준으로 분석합니다.
 */
@Service
public class PlayerAnalysisService {

    private final PlayerRepository playerRepository;

    private final PlayerStatsRepository playerStatsRepository;

    private final FantasyScoreService fantasyScoreService;


    public PlayerAnalysisService(

            PlayerRepository playerRepository,

            PlayerStatsRepository playerStatsRepository,

            FantasyScoreService fantasyScoreService
    ) {

        this.playerRepository =
                playerRepository;

        this.playerStatsRepository =
                playerStatsRepository;

        this.fantasyScoreService =
                fantasyScoreService;
    }


    /**
     * 기존 코드 호환용
     *
     * 최신 시즌 + PPR
     */
    public PlayerAnalysis analyzePlayer(
            Long playerId
    ) {

        return analyzePlayer(

                playerId,

                ScoringFormat.PPR
        );
    }


    /**
     * 최신 시즌 +
     * 사용자가 선택한 Scoring Format
     */
    public PlayerAnalysis analyzePlayer(

            Long playerId,

            ScoringFormat scoringFormat
    ) {

        Player player =
                findPlayer(
                        playerId
                );


        PlayerStats latestStats =
                playerStatsRepository
                        .findTopByPlayerIdOrderBySeasonDescWeekDesc(
                                playerId
                        )
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Player stats not found"
                                )

                        );


        return analyzeSeason(

                player,

                latestStats.getSeason(),

                scoringFormat
        );
    }


    /**
     * 기존 코드 호환용
     *
     * 특정 시즌 + PPR
     */
    public PlayerAnalysis analyzePlayer(

            Long playerId,

            int season
    ) {

        return analyzePlayer(

                playerId,

                season,

                ScoringFormat.PPR
        );
    }


    /**
     * 특정 시즌 +
     * 특정 Scoring Format
     */
    public PlayerAnalysis analyzePlayer(

            Long playerId,

            int season,

            ScoringFormat scoringFormat
    ) {

        Player player =
                findPlayer(
                        playerId
                );


        return analyzeSeason(

                player,

                season,

                scoringFormat
        );
    }


    private PlayerAnalysis analyzeSeason(

            Player player,

            int season,

            ScoringFormat scoringFormat
    ) {


        List<PlayerStats> stats =
                playerStatsRepository
                        .findByPlayerIdAndSeasonOrderByWeekAsc(

                                player.getId(),

                                season
                        );


        if (stats.isEmpty()) {

            throw new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Player stats not found for season "
                            + season
            );
        }


        /*
         * 시즌 평균
         */
        double seasonAverage =
                stats.stream()

                        .mapToDouble(

                                stat ->
                                        fantasyScoreService
                                                .calculateScore(

                                                        stat,

                                                        scoringFormat
                                                )

                        )

                        .average()

                        .orElse(0);


        /*
         * 최고점
         */
        double highestScore =
                stats.stream()

                        .mapToDouble(

                                stat ->
                                        fantasyScoreService
                                                .calculateScore(

                                                        stat,

                                                        scoringFormat
                                                )

                        )

                        .max()

                        .orElse(0);


        /*
         * 최저점
         */
        double lowestScore =
                stats.stream()

                        .mapToDouble(

                                stat ->
                                        fantasyScoreService
                                                .calculateScore(

                                                        stat,

                                                        scoringFormat
                                                )

                        )

                        .min()

                        .orElse(0);


        /*
         * 최근 3경기
         */
        int startIndex =
                Math.max(

                        0,

                        stats.size() - 3
                );


        List<PlayerStats> recentStats =
                stats.subList(

                        startIndex,

                        stats.size()
                );


        double recentAverage =
                recentStats.stream()

                        .mapToDouble(

                                stat ->
                                        fantasyScoreService
                                                .calculateScore(

                                                        stat,

                                                        scoringFormat
                                                )

                        )

                        .average()

                        .orElse(0);


        /*
         * Trend 계산
         */
        String trend;


        if (

                recentAverage
                        > seasonAverage + 1.0

        ) {

            trend = "UP";

        } else if (

                recentAverage
                        < seasonAverage - 1.0

        ) {

            trend = "DOWN";

        } else {

            trend = "STABLE";
        }


        return new PlayerAnalysis(

                player.getId(),

                player.getName(),

                stats.size(),

                round(
                        seasonAverage
                ),

                round(
                        recentAverage
                ),

                round(
                        highestScore
                ),

                round(
                        lowestScore
                ),

                trend
        );
    }


    private Player findPlayer(
            Long playerId
    ) {

        return playerRepository
                .findById(playerId)
                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "Player not found"
                        )

                );
    }


    private double round(
            double value
    ) {

        return Math.round(

                value * 100.0

        ) / 100.0;
    }
}