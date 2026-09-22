package com.taegun.fantasy.player;


/**
 * 경기 기록을 API로 전달하기 위한 DTO입니다.
 *
 * DB의 Raw Stats와
 * 요청한 Scoring Format으로 계산된
 * Fantasy Points를 함께 반환합니다.
 */
public class PlayerStatsResponse {

    private final Long id;

    private final Long playerId;

    private final int season;

    private final int week;

    private final double fantasyPoints;


    private final int passingYards;

    private final int passingTouchdowns;

    private final int interceptions;


    private final int rushingYards;

    private final int rushingTouchdowns;


    private final int receptions;

    private final int receivingYards;

    private final int receivingTouchdowns;


    private final int fumblesLost;

    private final int touchdowns;


    public PlayerStatsResponse(

            Long id,

            Long playerId,

            int season,

            int week,

            double fantasyPoints,

            int passingYards,

            int passingTouchdowns,

            int interceptions,

            int rushingYards,

            int rushingTouchdowns,

            int receptions,

            int receivingYards,

            int receivingTouchdowns,

            int fumblesLost,

            int touchdowns
    ) {

        this.id =
                id;

        this.playerId =
                playerId;

        this.season =
                season;

        this.week =
                week;

        this.fantasyPoints =
                fantasyPoints;


        this.passingYards =
                passingYards;

        this.passingTouchdowns =
                passingTouchdowns;

        this.interceptions =
                interceptions;


        this.rushingYards =
                rushingYards;

        this.rushingTouchdowns =
                rushingTouchdowns;


        this.receptions =
                receptions;

        this.receivingYards =
                receivingYards;

        this.receivingTouchdowns =
                receivingTouchdowns;


        this.fumblesLost =
                fumblesLost;

        this.touchdowns =
                touchdowns;
    }


    public static PlayerStatsResponse from(

            PlayerStats stats,

            double fantasyPoints
    ) {

        return new PlayerStatsResponse(

                stats.getId(),

                stats.getPlayer().getId(),

                stats.getSeason(),

                stats.getWeek(),

                fantasyPoints,

                stats.getPassingYards(),

                stats.getPassingTouchdowns(),

                stats.getInterceptions(),

                stats.getRushingYards(),

                stats.getRushingTouchdowns(),

                stats.getReceptions(),

                stats.getReceivingYards(),

                stats.getReceivingTouchdowns(),

                stats.getFumblesLost(),

                stats.getTouchdowns()
        );
    }


    public Long getId() {
        return id;
    }


    public Long getPlayerId() {
        return playerId;
    }


    public int getSeason() {
        return season;
    }


    public int getWeek() {
        return week;
    }


    public double getFantasyPoints() {
        return fantasyPoints;
    }


    public int getPassingYards() {
        return passingYards;
    }


    public int getPassingTouchdowns() {
        return passingTouchdowns;
    }


    public int getInterceptions() {
        return interceptions;
    }


    public int getRushingYards() {
        return rushingYards;
    }


    public int getRushingTouchdowns() {
        return rushingTouchdowns;
    }


    public int getReceptions() {
        return receptions;
    }


    public int getReceivingYards() {
        return receivingYards;
    }


    public int getReceivingTouchdowns() {
        return receivingTouchdowns;
    }


    public int getFumblesLost() {
        return fumblesLost;
    }


    public int getTouchdowns() {
        return touchdowns;
    }
}