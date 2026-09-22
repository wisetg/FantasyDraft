package com.taegun.fantasy.league;

import com.taegun.fantasy.player.ScoringFormat;

import java.util.List;


/**
 * 리그 상세 정보 +
 * 모든 Fantasy Team +
 * 각 Team Roster
 */
public class FantasyLeagueDetailResponse {

    private final Long id;

    private final FantasyPlatform platform;

    private final String externalLeagueId;

    private final String name;

    private final int season;

    private final int totalTeams;

    private final String status;

    private final ScoringFormat scoringFormat;

    private final List<FantasyTeamResponse>
            teams;


    public FantasyLeagueDetailResponse(

            Long id,

            FantasyPlatform platform,

            String externalLeagueId,

            String name,

            int season,

            int totalTeams,

            String status,

            ScoringFormat scoringFormat,

            List<FantasyTeamResponse> teams
    ) {

        this.id =
                id;

        this.platform =
                platform;

        this.externalLeagueId =
                externalLeagueId;

        this.name =
                name;

        this.season =
                season;

        this.totalTeams =
                totalTeams;

        this.status =
                status;

        this.scoringFormat =
                scoringFormat;

        this.teams =
                teams;
    }


    public Long getId() {
        return id;
    }


    public FantasyPlatform getPlatform() {
        return platform;
    }


    public String getExternalLeagueId() {
        return externalLeagueId;
    }


    public String getName() {
        return name;
    }


    public int getSeason() {
        return season;
    }


    public int getTotalTeams() {
        return totalTeams;
    }


    public String getStatus() {
        return status;
    }


    public ScoringFormat getScoringFormat() {
        return scoringFormat;
    }


    public List<FantasyTeamResponse> getTeams() {
        return teams;
    }
}