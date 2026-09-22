package com.taegun.fantasy.league;

import com.taegun.fantasy.player.ScoringFormat;

import jakarta.persistence.*;


/**
 * Fantasy Football League 자체를 저장합니다.
 *
 * 예:
 *
 * 태건님의 Sleeper League
 * 또는
 * ESPN League
 */
@Entity
@Table(
        name = "fantasy_leagues",

        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_fantasy_league_platform_external",
                        columnNames = {
                                "platform",
                                "external_league_id"
                        }
                )
        }
)
public class FantasyLeague {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    /**
     * SLEEPER / ESPN
     */
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private FantasyPlatform platform;


    /**
     * 외부 플랫폼의 League ID
     *
     * Sleeper의 league_id 등이 들어갑니다.
     */
    @Column(
            name = "external_league_id",
            nullable = false,
            length = 100
    )
    private String externalLeagueId;


    /**
     * League 이름
     */
    @Column(
            nullable = false
    )
    private String name;


    /**
     * 시즌
     *
     * 예:
     * 2026
     */
    private int season;


    /**
     * 참가 Fantasy Team 수
     */
    private int totalTeams;


    /**
     * League 상태
     *
     * 예:
     *
     * pre_draft
     * drafting
     * in_season
     * complete
     */
    private String status;


    /**
     * 우리 분석 시스템에서 사용할
     * 기본 Scoring Format
     *
     * PPR
     * HALF_PPR
     * STANDARD
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "scoring_format",
            nullable = false,
            length = 30
    )
    private ScoringFormat scoringFormat;


    /**
     * 외부 플랫폼의 원본 scoring_settings를
     * JSON 문자열 그대로 보존합니다.
     *
     * 지금은 PPR/Half/Standard만 쓰지만
     * 나중에는 커스텀 리그 규칙까지
     * 계산하기 위해 사용합니다.
     */
    @Column(
            name = "scoring_settings_json",
            columnDefinition = "TEXT"
    )
    private String scoringSettingsJson;


    protected FantasyLeague() {
    }


    public FantasyLeague(

            FantasyPlatform platform,

            String externalLeagueId,

            String name,

            int season,

            int totalTeams,

            String status,

            ScoringFormat scoringFormat,

            String scoringSettingsJson
    ) {

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

        this.scoringSettingsJson =
                scoringSettingsJson;
    }


    /**
     * 외부 플랫폼에서 다시 동기화했을 때
     * 변경된 정보를 갱신합니다.
     */
    public void updateFromExternal(

            String name,

            int season,

            int totalTeams,

            String status,

            ScoringFormat scoringFormat,

            String scoringSettingsJson
    ) {

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

        this.scoringSettingsJson =
                scoringSettingsJson;
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


    public String getScoringSettingsJson() {
        return scoringSettingsJson;
    }
}