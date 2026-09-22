package com.taegun.fantasy.league;

import jakarta.persistence.*;


/**
 * Fantasy League 안의 한 팀입니다.
 *
 * Sleeper에서는 roster_id가
 * 사실상 League 내부 Team 식별자 역할을 합니다.
 */
@Entity
@Table(
        name = "fantasy_teams",

        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_fantasy_team_league_external",
                        columnNames = {
                                "fantasy_league_id",
                                "external_team_id"
                        }
                )
        }
)
public class FantasyTeam {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    /**
     * 어떤 Fantasy League에
     * 속한 팀인지 나타냅니다.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "fantasy_league_id",
            nullable = false
    )
    private FantasyLeague fantasyLeague;


    /**
     * 외부 플랫폼에서 사용하는
     * Team / Roster ID
     *
     * Sleeper에서는 roster_id를
     * 문자열로 변환해서 저장할 예정입니다.
     */
    @Column(
            name = "external_team_id",
            nullable = false,
            length = 100
    )
    private String externalTeamId;


    /**
     * 외부 플랫폼 User ID
     *
     * Sleeper에서는 owner_id
     */
    @Column(
            name = "owner_external_user_id",
            length = 100
    )
    private String ownerExternalUserId;


    /**
     * 사용자 화면에 보여줄 소유자 이름
     *
     * Sleeper display_name 등을 저장합니다.
     */
    private String ownerDisplayName;


    /**
     * Fantasy Team 이름
     *
     * 별도 이름이 없으면
     * ownerDisplayName 등을 사용할 수 있습니다.
     */
    private String teamName;


    protected FantasyTeam() {
    }


    public FantasyTeam(

            FantasyLeague fantasyLeague,

            String externalTeamId,

            String ownerExternalUserId,

            String ownerDisplayName,

            String teamName
    ) {

        this.fantasyLeague =
                fantasyLeague;

        this.externalTeamId =
                externalTeamId;

        this.ownerExternalUserId =
                ownerExternalUserId;

        this.ownerDisplayName =
                ownerDisplayName;

        this.teamName =
                teamName;
    }


    public void updateFromExternal(

            String ownerExternalUserId,

            String ownerDisplayName,

            String teamName
    ) {

        this.ownerExternalUserId =
                ownerExternalUserId;

        this.ownerDisplayName =
                ownerDisplayName;

        this.teamName =
                teamName;
    }


    public Long getId() {
        return id;
    }


    public FantasyLeague getFantasyLeague() {
        return fantasyLeague;
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
}