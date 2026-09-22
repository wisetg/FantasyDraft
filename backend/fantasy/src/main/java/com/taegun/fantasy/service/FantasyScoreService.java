package com.taegun.fantasy.service;

import com.taegun.fantasy.player.PlayerStats;
import com.taegun.fantasy.player.ScoringFormat;

import org.springframework.stereotype.Service;


/**
 * 경기 기록을 기반으로
 * Fantasy Football 점수를 계산합니다.
 *
 * 지원 방식:
 *
 * PPR
 * HALF_PPR
 * STANDARD
 */
@Service
public class FantasyScoreService {


    /**
     * PlayerStats 객체를 받아서
     * Fantasy Points를 계산합니다.
     */
    public double calculateScore(
            PlayerStats stats,
            ScoringFormat scoringFormat
    ) {

        return calculateScore(

                stats.getPassingYards(),

                stats.getPassingTouchdowns(),

                stats.getInterceptions(),

                stats.getRushingYards(),

                stats.getRushingTouchdowns(),

                stats.getReceptions(),

                stats.getReceivingYards(),

                stats.getReceivingTouchdowns(),

                stats.getFumblesLost(),

                scoringFormat
        );
    }


    /**
     * Raw Stats를 기반으로
     * 실제 Fantasy Points를 계산합니다.
     */
    public double calculateScore(

            int passingYards,
            int passingTouchdowns,
            int interceptions,

            int rushingYards,
            int rushingTouchdowns,

            int receptions,
            int receivingYards,
            int receivingTouchdowns,

            int fumblesLost,

            ScoringFormat scoringFormat
    ) {


        /*
         * Passing
         *
         * Passing Yard
         * 25 yards = 1 point
         *
         * 즉:
         * 1 yard = 0.04
         */
        double passingScore =

                passingYards * 0.04

                        + passingTouchdowns * 4.0

                        - interceptions * 2.0;


        /*
         * Rushing
         *
         * 10 yards = 1 point
         *
         * TD = 6 points
         */
        double rushingScore =

                rushingYards * 0.1

                        + rushingTouchdowns * 6.0;


        /*
         * Receiving
         *
         * Reception 점수는
         * ScoringFormat에 따라 달라집니다.
         */
        double receivingScore =

                receptions
                        * scoringFormat
                        .getReceptionPoint()

                        + receivingYards * 0.1

                        + receivingTouchdowns * 6.0;


        /*
         * Fumble Lost
         */
        double turnoverScore =

                fumblesLost * -2.0;


        double totalScore =

                passingScore

                        + rushingScore

                        + receivingScore

                        + turnoverScore;


        return round(
                totalScore
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