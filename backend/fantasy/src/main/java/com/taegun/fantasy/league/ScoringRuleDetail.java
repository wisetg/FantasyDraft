package com.taegun.fantasy.league;


/**
 * Fantasy League의 Scoring Rule 하나를
 * Frontend로 전달하기 위한 DTO입니다.
 */
public class ScoringRuleDetail {

    private final String key;

    private final double value;

    private final boolean supported;


    public ScoringRuleDetail(

            String key,

            double value,

            boolean supported
    ) {

        this.key =
                key;

        this.value =
                value;

        this.supported =
                supported;
    }


    public String getKey() {

        return key;
    }


    public double getValue() {

        return value;
    }


    public boolean isSupported() {

        return supported;
    }
}