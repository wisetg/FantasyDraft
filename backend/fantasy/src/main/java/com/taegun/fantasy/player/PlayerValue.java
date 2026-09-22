package com.taegun.fantasy.player;


/**
 * Fantasy Player의 Trade Value 결과입니다.
 *
 * 기존 Trade Value 정보와 함께
 * League-specific Replacement Level 정보를
 * 포함할 수 있습니다.
 */
public class PlayerValue {

    private final Long playerId;

    private final String playerName;


    /**
     * 최종 Trade Value
     */
    private final double valueScore;


    /**
     * 시즌 경기당 평균 Fantasy Point
     */
    private final double seasonAverage;


    /**
     * 최근 3경기 평균
     */
    private final double recentAverage;


    /**
     * 일관성 점수
     */
    private final double consistencyScore;


    /**
     * 최근 Trend 보너스
     */
    private final double trendScore;


    /**
     * UP / STABLE / DOWN
     */
    private final String trend;


    // =========================================================
    // Trade Value V2
    // =========================================================

    /**
     * Player Position
     *
     * QB / RB / WR / TE / K / DEF
     */
    private final String position;


    /**
     * 분석에 사용한 경기 수
     */
    private final int gamesPlayed;


    /**
     * 해당 League에서 같은 Position의
     * Replacement Player 평균 점수
     */
    private final double replacementAverage;


    /**
     * Season Average - Replacement Average
     */
    private final double valueAboveReplacement;


    /**
     * Recent Average - Replacement Average
     */
    private final double recentValueAboveReplacement;


    /**
     * 기존 PlayerValueService와의 호환을 위한 생성자입니다.
     */
    public PlayerValue(
            Long playerId,
            String playerName,
            double valueScore,
            double seasonAverage,
            double recentAverage,
            double consistencyScore,
            double trendScore,
            String trend
    ) {

        this(
                playerId,
                playerName,
                valueScore,
                seasonAverage,
                recentAverage,
                consistencyScore,
                trendScore,
                trend,

                null,
                0,
                0.0,
                0.0,
                0.0
        );
    }


    /**
     * Trade Value V2 생성자입니다.
     */
    public PlayerValue(
            Long playerId,
            String playerName,
            double valueScore,
            double seasonAverage,
            double recentAverage,
            double consistencyScore,
            double trendScore,
            String trend,

            String position,
            int gamesPlayed,
            double replacementAverage,
            double valueAboveReplacement,
            double recentValueAboveReplacement
    ) {

        this.playerId =
                playerId;

        this.playerName =
                playerName;

        this.valueScore =
                valueScore;

        this.seasonAverage =
                seasonAverage;

        this.recentAverage =
                recentAverage;

        this.consistencyScore =
                consistencyScore;

        this.trendScore =
                trendScore;

        this.trend =
                trend;

        this.position =
                position;

        this.gamesPlayed =
                gamesPlayed;

        this.replacementAverage =
                replacementAverage;

        this.valueAboveReplacement =
                valueAboveReplacement;

        this.recentValueAboveReplacement =
                recentValueAboveReplacement;
    }


    public Long getPlayerId() {
        return playerId;
    }


    public String getPlayerName() {
        return playerName;
    }


    public double getValueScore() {
        return valueScore;
    }


    public double getSeasonAverage() {
        return seasonAverage;
    }


    public double getRecentAverage() {
        return recentAverage;
    }


    public double getConsistencyScore() {
        return consistencyScore;
    }


    public double getTrendScore() {
        return trendScore;
    }


    public String getTrend() {
        return trend;
    }


    public String getPosition() {
        return position;
    }


    public int getGamesPlayed() {
        return gamesPlayed;
    }


    public double getReplacementAverage() {
        return replacementAverage;
    }


    public double getValueAboveReplacement() {
        return valueAboveReplacement;
    }


    public double getRecentValueAboveReplacement() {
        return recentValueAboveReplacement;
    }
}