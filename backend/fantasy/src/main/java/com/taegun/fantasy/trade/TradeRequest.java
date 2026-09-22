package com.taegun.fantasy.trade;

import com.taegun.fantasy.player.ScoringFormat;

import java.util.List;


/**
 * 트레이드 분석 요청 DTO
 */
public class TradeRequest {

    private List<Long> teamAPlayerIds;

    private List<Long> teamBPlayerIds;


    /**
     * 실제 Fantasy League ID
     *
     * 값이 존재하면
     * ScoringFormat 대신
     * League의 실제 scoring_settings를 사용합니다.
     */
    private Long fantasyLeagueId;


    /**
     * 기존 수동 분석과의
     * 호환성을 위해 유지합니다.
     */
    private Integer season;


    private ScoringFormat scoringFormat =
            ScoringFormat.PPR;


    public TradeRequest() {
    }


    public List<Long> getTeamAPlayerIds() {

        return teamAPlayerIds;
    }


    public void setTeamAPlayerIds(
            List<Long> teamAPlayerIds
    ) {

        this.teamAPlayerIds =
                teamAPlayerIds;
    }


    public List<Long> getTeamBPlayerIds() {

        return teamBPlayerIds;
    }


    public void setTeamBPlayerIds(
            List<Long> teamBPlayerIds
    ) {

        this.teamBPlayerIds =
                teamBPlayerIds;
    }


    public Long getFantasyLeagueId() {

        return fantasyLeagueId;
    }


    public void setFantasyLeagueId(
            Long fantasyLeagueId
    ) {

        this.fantasyLeagueId =
                fantasyLeagueId;
    }


    public Integer getSeason() {

        return season;
    }


    public void setSeason(
            Integer season
    ) {

        this.season =
                season;
    }


    public ScoringFormat getScoringFormat() {

        return scoringFormat;
    }


    public void setScoringFormat(
            ScoringFormat scoringFormat
    ) {

        this.scoringFormat =
                scoringFormat;
    }
}