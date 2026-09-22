package com.taegun.fantasy.integration.sleeper;

/**
 * Sleeper 선수 동기화 결과를
 * API에 반환하기 위한 DTO입니다.
 */
public class SleeperPlayerSyncResult {

    private final int receivedPlayers;

    private final int eligiblePlayers;

    private final int createdPlayers;

    private final int updatedPlayers;

    private final int linkedExistingPlayers;

    private final int skippedPlayers;


    public SleeperPlayerSyncResult(

            int receivedPlayers,

            int eligiblePlayers,

            int createdPlayers,

            int updatedPlayers,

            int linkedExistingPlayers,

            int skippedPlayers
    ) {

        this.receivedPlayers =
                receivedPlayers;

        this.eligiblePlayers =
                eligiblePlayers;

        this.createdPlayers =
                createdPlayers;

        this.updatedPlayers =
                updatedPlayers;

        this.linkedExistingPlayers =
                linkedExistingPlayers;

        this.skippedPlayers =
                skippedPlayers;
    }


    public int getReceivedPlayers() {
        return receivedPlayers;
    }


    public int getEligiblePlayers() {
        return eligiblePlayers;
    }


    public int getCreatedPlayers() {
        return createdPlayers;
    }


    public int getUpdatedPlayers() {
        return updatedPlayers;
    }


    public int getLinkedExistingPlayers() {
        return linkedExistingPlayers;
    }


    public int getSkippedPlayers() {
        return skippedPlayers;
    }
}