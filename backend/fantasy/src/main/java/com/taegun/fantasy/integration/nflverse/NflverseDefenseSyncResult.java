package com.taegun.fantasy.integration.nflverse;


/**
 * NFL Team Defense 동기화 결과입니다.
 *
 * Team Stats + PBP Points Allowed를
 * 한 번에 처리합니다.
 */
public class NflverseDefenseSyncResult {

    /**
     * 동기화 시즌
     */
    private final int season;


    /**
     * nflverse Team Stats Row 수
     */
    private final int sourceRows;


    /**
     * 읽은 nflverse PBP Row 수
     */
    private final int pbpRows;


    /**
     * 우리 DEF Player와 매칭된 Team Stats 수
     */
    private final int matchedDefenses;


    /**
     * 새로 만들어진 PlayerStats 수
     */
    private final int createdStats;


    /**
     * 기존 PlayerStats 갱신 수
     */
    private final int updatedStats;


    /**
     * Points Allowed가 저장된
     * Defense Week 수
     */
    private final int pointsAllowedUpdated;


    /**
     * 우리 DB에서 DEF Player를 찾지 못한 수
     */
    private final int skippedNoDefensePlayer;


    public NflverseDefenseSyncResult(
            int season,
            int sourceRows,
            int pbpRows,
            int matchedDefenses,
            int createdStats,
            int updatedStats,
            int pointsAllowedUpdated,
            int skippedNoDefensePlayer
    ) {

        this.season = season;
        this.sourceRows = sourceRows;
        this.pbpRows = pbpRows;
        this.matchedDefenses = matchedDefenses;
        this.createdStats = createdStats;
        this.updatedStats = updatedStats;
        this.pointsAllowedUpdated = pointsAllowedUpdated;
        this.skippedNoDefensePlayer = skippedNoDefensePlayer;
    }


    public int getSeason() {
        return season;
    }


    public int getSourceRows() {
        return sourceRows;
    }


    public int getPbpRows() {
        return pbpRows;
    }


    public int getMatchedDefenses() {
        return matchedDefenses;
    }


    public int getCreatedStats() {
        return createdStats;
    }


    public int getUpdatedStats() {
        return updatedStats;
    }


    public int getPointsAllowedUpdated() {
        return pointsAllowedUpdated;
    }


    public int getSkippedNoDefensePlayer() {
        return skippedNoDefensePlayer;
    }
}