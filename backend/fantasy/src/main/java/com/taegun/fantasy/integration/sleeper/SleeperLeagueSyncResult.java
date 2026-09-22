package com.taegun.fantasy.integration.sleeper;

import com.taegun.fantasy.player.ScoringFormat;


/**
 * Sleeper League 동기화 결과입니다.
 */
public class SleeperLeagueSyncResult {

    private final Long fantasyLeagueId;

    private final Long myFantasyTeamId;

    private final String sleeperLeagueId;

    private final String leagueName;

    private final int season;

    private final ScoringFormat scoringFormat;

    private final int rostersReceived;

    private final int teamsCreated;

    private final int teamsUpdated;

    private final int rosterPlayersSaved;

    private final int skippedUnmappedPlayers;


    public SleeperLeagueSyncResult(

            Long fantasyLeagueId,

            Long myFantasyTeamId,

            String sleeperLeagueId,

            String leagueName,

            int season,

            ScoringFormat scoringFormat,

            int rostersReceived,

            int teamsCreated,

            int teamsUpdated,

            int rosterPlayersSaved,

            int skippedUnmappedPlayers
    ) {

        this.fantasyLeagueId =
                fantasyLeagueId;

        this.myFantasyTeamId =
                myFantasyTeamId;

        this.sleeperLeagueId =
                sleeperLeagueId;

        this.leagueName =
                leagueName;

        this.season =
                season;

        this.scoringFormat =
                scoringFormat;

        this.rostersReceived =
                rostersReceived;

        this.teamsCreated =
                teamsCreated;

        this.teamsUpdated =
                teamsUpdated;

        this.rosterPlayersSaved =
                rosterPlayersSaved;

        this.skippedUnmappedPlayers =
                skippedUnmappedPlayers;
    }


    public Long getFantasyLeagueId() {
        return fantasyLeagueId;
    }


    public Long getMyFantasyTeamId() {
        return myFantasyTeamId;
    }


    public String getSleeperLeagueId() {
        return sleeperLeagueId;
    }


    public String getLeagueName() {
        return leagueName;
    }


    public int getSeason() {
        return season;
    }


    public ScoringFormat getScoringFormat() {
        return scoringFormat;
    }


    public int getRostersReceived() {
        return rostersReceived;
    }


    public int getTeamsCreated() {
        return teamsCreated;
    }


    public int getTeamsUpdated() {
        return teamsUpdated;
    }


    public int getRosterPlayersSaved() {
        return rosterPlayersSaved;
    }


    public int getSkippedUnmappedPlayers() {
        return skippedUnmappedPlayers;
    }
}