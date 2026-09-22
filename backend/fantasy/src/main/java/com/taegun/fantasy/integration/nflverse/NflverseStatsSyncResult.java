package com.taegun.fantasy.integration.nflverse;


/**
 * nflverse 경기 Stats 동기화 결과입니다.
 */
public class NflverseStatsSyncResult {

    private final int season;

    private final int sourceRows;

    private final int regularSeasonRows;

    private final int matchedRows;

    private final int createdStats;

    private final int updatedStats;

    private final int skippedNoGsisMatch;

    private final int skippedUnsupportedPosition;


    public NflverseStatsSyncResult(

            int season,

            int sourceRows,

            int regularSeasonRows,

            int matchedRows,

            int createdStats,

            int updatedStats,

            int skippedNoGsisMatch,

            int skippedUnsupportedPosition
    ) {

        this.season =
                season;

        this.sourceRows =
                sourceRows;

        this.regularSeasonRows =
                regularSeasonRows;

        this.matchedRows =
                matchedRows;

        this.createdStats =
                createdStats;

        this.updatedStats =
                updatedStats;

        this.skippedNoGsisMatch =
                skippedNoGsisMatch;

        this.skippedUnsupportedPosition =
                skippedUnsupportedPosition;
    }


    public int getSeason() {
        return season;
    }


    public int getSourceRows() {
        return sourceRows;
    }


    public int getRegularSeasonRows() {
        return regularSeasonRows;
    }


    public int getMatchedRows() {
        return matchedRows;
    }


    public int getCreatedStats() {
        return createdStats;
    }


    public int getUpdatedStats() {
        return updatedStats;
    }


    public int getSkippedNoGsisMatch() {
        return skippedNoGsisMatch;
    }


    public int getSkippedUnsupportedPosition() {
        return skippedUnsupportedPosition;
    }
}