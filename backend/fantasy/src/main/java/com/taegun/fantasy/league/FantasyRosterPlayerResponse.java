package com.taegun.fantasy.league;


/**
 * Fantasy Roster 선수 Frontend 응답입니다.
 */
public class FantasyRosterPlayerResponse {

    private final Long playerId;

    private final String name;

    private final String position;

    private final String nflTeam;

    private final RosterPlayerStatus status;

    private final String lineupSlot;

    private final Integer lineupOrder;


    public FantasyRosterPlayerResponse(

            Long playerId,

            String name,

            String position,

            String nflTeam,

            RosterPlayerStatus status,

            String lineupSlot,

            Integer lineupOrder
    ) {

        this.playerId =
                playerId;

        this.name =
                name;

        this.position =
                position;

        this.nflTeam =
                nflTeam;

        this.status =
                status;

        this.lineupSlot =
                lineupSlot;

        this.lineupOrder =
                lineupOrder;
    }


    public static FantasyRosterPlayerResponse from(
            FantasyRosterPlayer rosterPlayer
    ) {

        return new FantasyRosterPlayerResponse(

                rosterPlayer
                        .getPlayer()
                        .getId(),

                rosterPlayer
                        .getPlayer()
                        .getName(),

                rosterPlayer
                        .getPlayer()
                        .getPosition(),

                rosterPlayer
                        .getPlayer()
                        .getTeam(),

                rosterPlayer
                        .getStatus(),

                rosterPlayer
                        .getLineupSlot(),

                rosterPlayer
                        .getLineupOrder()
        );
    }


    public Long getPlayerId() {
        return playerId;
    }


    public String getName() {
        return name;
    }


    public String getPosition() {
        return position;
    }


    public String getNflTeam() {
        return nflTeam;
    }


    public RosterPlayerStatus getStatus() {
        return status;
    }


    public String getLineupSlot() {
        return lineupSlot;
    }


    public Integer getLineupOrder() {
        return lineupOrder;
    }
}