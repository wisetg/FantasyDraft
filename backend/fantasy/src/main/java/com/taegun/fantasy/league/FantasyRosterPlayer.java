package com.taegun.fantasy.league;

import com.taegun.fantasy.player.Player;

import jakarta.persistence.*;


/**
 * Fantasy Team이 보유한 실제 NFL Player입니다.
 */
@Entity
@Table(
        name = "fantasy_roster_players",

        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_fantasy_roster_team_player",
                        columnNames = {
                                "fantasy_team_id",
                                "player_id"
                        }
                )
        }
)
public class FantasyRosterPlayer {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "fantasy_team_id",
            nullable = false
    )
    private FantasyTeam fantasyTeam;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "player_id",
            nullable = false
    )
    private Player player;


    /**
     * STARTER
     * BENCH
     * RESERVE
     */
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private RosterPlayerStatus status;


    /**
     * 실제 Sleeper 라인업 슬롯입니다.
     *
     * 예:
     *
     * QB
     * RB
     * WR
     * TE
     * FLEX
     * K
     * DEF
     *
     * 벤치 선수는 BENCH,
     * Reserve 선수는 RESERVE가 들어갑니다.
     */
    @Column(
            name = "lineup_slot",
            length = 30
    )
    private String lineupSlot;


    /**
     * 화면에 표시할 정렬 순서입니다.
     *
     * 값이 작을수록 먼저 표시됩니다.
     */
    @Column(
            name = "lineup_order"
    )
    private Integer lineupOrder;


    protected FantasyRosterPlayer() {
    }


    public FantasyRosterPlayer(

            FantasyTeam fantasyTeam,

            Player player,

            RosterPlayerStatus status,

            String lineupSlot,

            Integer lineupOrder
    ) {

        this.fantasyTeam =
                fantasyTeam;

        this.player =
                player;

        this.status =
                status;

        this.lineupSlot =
                lineupSlot;

        this.lineupOrder =
                lineupOrder;
    }


    public void updateRosterInfo(

            RosterPlayerStatus status,

            String lineupSlot,

            Integer lineupOrder
    ) {

        this.status =
                status;

        this.lineupSlot =
                lineupSlot;

        this.lineupOrder =
                lineupOrder;
    }


    public Long getId() {
        return id;
    }


    public FantasyTeam getFantasyTeam() {
        return fantasyTeam;
    }


    public Player getPlayer() {
        return player;
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