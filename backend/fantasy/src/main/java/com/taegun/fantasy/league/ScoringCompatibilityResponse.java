package com.taegun.fantasy.league;

import java.util.List;


/**
 * 현재 우리 분석 엔진이
 * 특정 League의 Scoring Rules를
 * 어느 정도 지원하는지 알려줍니다.
 */
public class ScoringCompatibilityResponse {

    private final Long fantasyLeagueId;

    private final String leagueName;

    private final boolean fullySupported;

    private final int activeRuleCount;

    private final int supportedRuleCount;

    private final int unsupportedRuleCount;

    private final List<ScoringRuleDetail>
            supportedRules;

    private final List<ScoringRuleDetail>
            unsupportedRules;


    public ScoringCompatibilityResponse(

            Long fantasyLeagueId,

            String leagueName,

            boolean fullySupported,

            int activeRuleCount,

            int supportedRuleCount,

            int unsupportedRuleCount,

            List<ScoringRuleDetail> supportedRules,

            List<ScoringRuleDetail> unsupportedRules
    ) {

        this.fantasyLeagueId =
                fantasyLeagueId;

        this.leagueName =
                leagueName;

        this.fullySupported =
                fullySupported;

        this.activeRuleCount =
                activeRuleCount;

        this.supportedRuleCount =
                supportedRuleCount;

        this.unsupportedRuleCount =
                unsupportedRuleCount;

        this.supportedRules =
                supportedRules;

        this.unsupportedRules =
                unsupportedRules;
    }


    public Long getFantasyLeagueId() {

        return fantasyLeagueId;
    }


    public String getLeagueName() {

        return leagueName;
    }


    public boolean isFullySupported() {

        return fullySupported;
    }


    public int getActiveRuleCount() {

        return activeRuleCount;
    }


    public int getSupportedRuleCount() {

        return supportedRuleCount;
    }


    public int getUnsupportedRuleCount() {

        return unsupportedRuleCount;
    }


    public List<ScoringRuleDetail>
    getSupportedRules() {

        return supportedRules;
    }


    public List<ScoringRuleDetail>
    getUnsupportedRules() {

        return unsupportedRules;
    }
}