package com.taegun.fantasy.player;

/**
 * 선수의 통계 분석 결과를 API로 반환하기 위한 DTO입니다.
 */
public class PlayerAnalysis {

    private Long playerId;
    private String playerName;

    private int gamesPlayed;

    private double seasonAverage;
    private double recentAverage;

    private double highestScore;
    private double lowestScore;

    private String trend;


    public PlayerAnalysis(
            Long playerId,
            String playerName,
            int gamesPlayed,
            double seasonAverage,
            double recentAverage,
            double highestScore,
            double lowestScore,
            String trend
    ) {

        this.playerId = playerId;
        this.playerName = playerName;
        this.gamesPlayed = gamesPlayed;
        this.seasonAverage = seasonAverage;
        this.recentAverage = recentAverage;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.trend = trend;
    }


    public Long getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public double getSeasonAverage() {
        return seasonAverage;
    }

    public double getRecentAverage() {
        return recentAverage;
    }

    public double getHighestScore() {
        return highestScore;
    }

    public double getLowestScore() {
        return lowestScore;
    }

    public String getTrend() {
        return trend;
    }
}