package com.taegun.fantasy.service;

import com.taegun.fantasy.player.PlayerAnalysis;
import com.taegun.fantasy.player.PlayerValue;
import com.taegun.fantasy.player.ScoringFormat;

import org.springframework.stereotype.Service;


/**
 * 선수의 Trade Value를 계산합니다.
 */
@Service
public class PlayerValueService {

    private final PlayerAnalysisService playerAnalysisService;


    public PlayerValueService(
            PlayerAnalysisService playerAnalysisService
    ) {

        this.playerAnalysisService =
                playerAnalysisService;
    }


    /**
     * 최신 시즌 + PPR
     */
    public PlayerValue calculatePlayerValue(
            Long playerId
    ) {

        return calculatePlayerValue(

                playerId,

                ScoringFormat.PPR
        );
    }


    /**
     * 최신 시즌 +
     * Scoring Format
     */
    public PlayerValue calculatePlayerValue(

            Long playerId,

            ScoringFormat scoringFormat
    ) {

        PlayerAnalysis analysis =
                playerAnalysisService
                        .analyzePlayer(

                                playerId,

                                scoringFormat
                        );


        return calculateValue(
                analysis
        );
    }


    /**
     * 특정 시즌 + PPR
     */
    public PlayerValue calculatePlayerValue(

            Long playerId,

            int season
    ) {

        return calculatePlayerValue(

                playerId,

                season,

                ScoringFormat.PPR
        );
    }


    /**
     * 특정 시즌 +
     * Scoring Format
     */
    public PlayerValue calculatePlayerValue(

            Long playerId,

            int season,

            ScoringFormat scoringFormat
    ) {

        PlayerAnalysis analysis =
                playerAnalysisService
                        .analyzePlayer(

                                playerId,

                                season,

                                scoringFormat
                        );


        return calculateValue(
                analysis
        );
    }


    private PlayerValue calculateValue(
            PlayerAnalysis analysis
    ) {


        double scoreRange =

                analysis.getHighestScore()

                        - analysis.getLowestScore();


        double consistencyScore =

                Math.max(

                        0,

                        30 - scoreRange
                );


        double trendScore;


        switch (
                analysis.getTrend()
        ) {

            case "UP":

                trendScore = 10;

                break;


            case "STABLE":

                trendScore = 7;

                break;


            case "DOWN":

                trendScore = 3;

                break;


            default:

                trendScore = 5;
        }


        double valueScore =

                analysis.getSeasonAverage()
                        * 0.40

                        + analysis.getRecentAverage()
                        * 0.30

                        + analysis.getHighestScore()
                        * 0.10

                        + consistencyScore
                        * 0.10

                        + trendScore
                        * 0.10;


        return new PlayerValue(

                analysis.getPlayerId(),

                analysis.getPlayerName(),

                round(
                        valueScore
                ),

                analysis.getSeasonAverage(),

                analysis.getRecentAverage(),

                round(
                        consistencyScore
                ),

                trendScore,

                analysis.getTrend()
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