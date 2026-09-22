package com.taegun.fantasy.controller;

import com.taegun.fantasy.league.FantasyLeagueDetailResponse;
import com.taegun.fantasy.league.ScoringCompatibilityResponse;

import com.taegun.fantasy.player.PlayerValue;

import com.taegun.fantasy.service.FantasyLeagueQueryService;
import com.taegun.fantasy.service.LeaguePlayerValueService;
import com.taegun.fantasy.service.LeagueScoringAuditService;

import org.springframework.web.bind.annotation.*;


/**
 * Fantasy League 관련 API입니다.
 */
@RestController
@RequestMapping(
        "/api/leagues"
)
public class FantasyLeagueController {

    private final FantasyLeagueQueryService
            fantasyLeagueQueryService;

    private final LeagueScoringAuditService
            leagueScoringAuditService;

    private final LeaguePlayerValueService
            leaguePlayerValueService;


    public FantasyLeagueController(
            FantasyLeagueQueryService fantasyLeagueQueryService,
            LeagueScoringAuditService leagueScoringAuditService,
            LeaguePlayerValueService leaguePlayerValueService
    ) {

        this.fantasyLeagueQueryService =
                fantasyLeagueQueryService;

        this.leagueScoringAuditService =
                leagueScoringAuditService;

        this.leaguePlayerValueService =
                leaguePlayerValueService;
    }


    /**
     * =========================================================
     * League 상세
     * =========================================================
     *
     * GET
     *
     * /api/leagues/1
     */
    @GetMapping(
            "/{id}"
    )
    public FantasyLeagueDetailResponse getLeagueDetail(
            @PathVariable Long id
    ) {

        return fantasyLeagueQueryService
                .getLeagueDetail(
                        id
                );
    }


    /**
     * =========================================================
     * Scoring Compatibility
     * =========================================================
     *
     * GET
     *
     * /api/leagues/1/scoring-compatibility
     */
    @GetMapping(
            "/{id}/scoring-compatibility"
    )
    public ScoringCompatibilityResponse
    getScoringCompatibility(
            @PathVariable Long id
    ) {

        return leagueScoringAuditService
                .analyzeLeague(
                        id
                );
    }


    /**
     * =========================================================
     * League-specific Player Value
     * =========================================================
     *
     * GET
     *
     * /api/leagues/1/players/123/value
     *
     * 실제 League:
     *
     * Scoring Settings
     * Roster Depth
     * Position Scarcity
     * Replacement Level
     *
     * 을 반영한 Player Value입니다.
     */
    @GetMapping(
            "/{leagueId}/players/{playerId}/value"
    )
    public PlayerValue getLeaguePlayerValue(

            @PathVariable
            Long leagueId,

            @PathVariable
            Long playerId
    ) {

        return leaguePlayerValueService
                .calculatePlayerValue(
                        playerId,
                        leagueId
                );
    }
}