package com.taegun.fantasy.integration.sleeper;

import com.taegun.fantasy.player.ScoringFormat;


/**
 * 사용자가 가지고 있는 Sleeper League를
 * 화면에 보여주기 위한 DTO입니다.
 */
public class SleeperLeaguePreview {

    private final String leagueId;

    private final String name;

    private final int season;

    private final int totalTeams;

    private final String status;

    private final ScoringFormat scoringFormat;

    /**
     * 실제 Reception 1개당 점수입니다.
     *
     * 예:
     *
     * PPR      = 1.0
     * Half-PPR = 0.5
     * Standard = 0.0
     */
    private final double receptionPoints;


    public SleeperLeaguePreview(

            String leagueId,

            String name,

            int season,

            int totalTeams,

            String status,

            ScoringFormat scoringFormat,

            double receptionPoints
    ) {

        this.leagueId =
                leagueId;

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

        this.receptionPoints =
                receptionPoints;
    }


    public String getLeagueId() {
        return leagueId;
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


    public double getReceptionPoints() {
        return receptionPoints;
    }
}