package com.taegun.fantasy.trade;

import com.taegun.fantasy.player.PlayerValue;

import java.util.List;

/**
 * 트레이드 분석 결과 DTO입니다.
 */
public class TradeResult {

    private double teamAValue;
    private double teamBValue;

    private double difference;
    private double fairness;

    private String message;

    private List<PlayerValue> teamAPlayerValues;
    private List<PlayerValue> teamBPlayerValues;

    private List<String> warnings;


    public TradeResult(
            double teamAValue,
            double teamBValue,
            double difference,
            double fairness,
            String message
    ) {

        this.teamAValue = teamAValue;
        this.teamBValue = teamBValue;
        this.difference = difference;
        this.fairness = fairness;
        this.message = message;
        this.teamAPlayerValues = List.of();
        this.teamBPlayerValues = List.of();
        this.warnings = List.of();
    }


    public TradeResult(
            double teamAValue,
            double teamBValue,
            double difference,
            double fairness,
            String message,
            List<PlayerValue> teamAPlayerValues,
            List<PlayerValue> teamBPlayerValues,
            List<String> warnings
    ) {

        this.teamAValue = teamAValue;
        this.teamBValue = teamBValue;
        this.difference = difference;
        this.fairness = fairness;
        this.message = message;
        this.teamAPlayerValues = List.copyOf(teamAPlayerValues);
        this.teamBPlayerValues = List.copyOf(teamBPlayerValues);
        this.warnings = List.copyOf(warnings);
    }


    public double getTeamAValue() {
        return teamAValue;
    }

    public double getTeamBValue() {
        return teamBValue;
    }

    public double getDifference() {
        return difference;
    }

    public double getFairness() {
        return fairness;
    }

    public String getMessage() {
        return message;
    }

    public List<PlayerValue> getTeamAPlayerValues() {
        return teamAPlayerValues;
    }

    public List<PlayerValue> getTeamBPlayerValues() {
        return teamBPlayerValues;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
