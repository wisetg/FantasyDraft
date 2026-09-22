package com.taegun.fantasy.player;

/**
 * Fantasy Football 점수 계산 방식입니다.
 *
 * PPR
 *      Reception 1개 = 1점
 *
 * HALF_PPR
 *      Reception 1개 = 0.5점
 *
 * STANDARD
 *      Reception 자체에는 점수를 주지 않습니다.
 */
public enum ScoringFormat {

    PPR(1.0),

    HALF_PPR(0.5),

    STANDARD(0.0);


    private final double receptionPoint;


    ScoringFormat(
            double receptionPoint
    ) {

        this.receptionPoint =
                receptionPoint;
    }


    public double getReceptionPoint() {

        return receptionPoint;
    }
}