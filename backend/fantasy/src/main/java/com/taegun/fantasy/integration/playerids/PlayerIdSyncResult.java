package com.taegun.fantasy.integration.playerids;

/**
 * Sleeper ID → GSIS ID 동기화 결과입니다.
 */
public class PlayerIdSyncResult {

    private final int sourceRows;

    private final int usableRows;

    private final int matchedPlayers;

    private final int createdMappings;

    private final int updatedMappings;

    private final int alreadyCurrent;

    private final int skippedNoSleeperMatch;

    private final int conflicts;


    public PlayerIdSyncResult(

            int sourceRows,

            int usableRows,

            int matchedPlayers,

            int createdMappings,

            int updatedMappings,

            int alreadyCurrent,

            int skippedNoSleeperMatch,

            int conflicts
    ) {

        this.sourceRows =
                sourceRows;

        this.usableRows =
                usableRows;

        this.matchedPlayers =
                matchedPlayers;

        this.createdMappings =
                createdMappings;

        this.updatedMappings =
                updatedMappings;

        this.alreadyCurrent =
                alreadyCurrent;

        this.skippedNoSleeperMatch =
                skippedNoSleeperMatch;

        this.conflicts =
                conflicts;
    }


    public int getSourceRows() {
        return sourceRows;
    }


    public int getUsableRows() {
        return usableRows;
    }


    public int getMatchedPlayers() {
        return matchedPlayers;
    }


    public int getCreatedMappings() {
        return createdMappings;
    }


    public int getUpdatedMappings() {
        return updatedMappings;
    }


    public int getAlreadyCurrent() {
        return alreadyCurrent;
    }


    public int getSkippedNoSleeperMatch() {
        return skippedNoSleeperMatch;
    }


    public int getConflicts() {
        return conflicts;
    }
}