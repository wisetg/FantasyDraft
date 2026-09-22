package com.taegun.fantasy.league;

import java.util.List;


/**
 * Fantasy Team + Roster 응답입니다.
 */
public class FantasyTeamResponse {

    private final Long id;

    private final String externalTeamId;

    private final String ownerExternalUserId;

    private final String ownerDisplayName;

    private final String teamName;

    private final List<FantasyRosterPlayerResponse>
            roster;


    public FantasyTeamResponse(

            Long id,

            String externalTeamId,

            String ownerExternalUserId,

            String ownerDisplayName,

            String teamName,

            List<FantasyRosterPlayerResponse> roster
    ) {

        this.id =
                id;

        this.externalTeamId =
                externalTeamId;

        this.ownerExternalUserId =
                ownerExternalUserId;

        this.ownerDisplayName =
                ownerDisplayName;

        this.teamName =
                teamName;

        this.roster =
                roster;
    }


    public Long getId() {
        return id;
    }


    public String getExternalTeamId() {
        return externalTeamId;
    }


    public String getOwnerExternalUserId() {
        return ownerExternalUserId;
    }


    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }


    public String getTeamName() {
        return teamName;
    }


    public List<FantasyRosterPlayerResponse>
    getRoster() {

        return roster;
    }
}