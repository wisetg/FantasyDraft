package com.taegun.fantasy.controller;

import com.taegun.fantasy.integration.sleeper.SleeperLeaguePreview;
import com.taegun.fantasy.integration.sleeper.SleeperLeagueSyncResult;
import com.taegun.fantasy.integration.sleeper.SleeperLeagueSyncService;
import com.taegun.fantasy.integration.sleeper.SleeperPlayerSyncResult;
import com.taegun.fantasy.integration.sleeper.SleeperPlayerSyncService;

import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Sleeper 외부 서비스 연동 API입니다.
 */
@RestController
@RequestMapping(
        "/api/integrations/sleeper"
)
public class SleeperIntegrationController {

    private final SleeperPlayerSyncService
            sleeperPlayerSyncService;

    private final SleeperLeagueSyncService
            sleeperLeagueSyncService;


    public SleeperIntegrationController(

            SleeperPlayerSyncService
                    sleeperPlayerSyncService,

            SleeperLeagueSyncService
                    sleeperLeagueSyncService
    ) {

        this.sleeperPlayerSyncService =
                sleeperPlayerSyncService;

        this.sleeperLeagueSyncService =
                sleeperLeagueSyncService;
    }


    /**
     * ===================================
     * Sleeper NFL 선수 동기화
     * ===================================
     *
     * POST
     *
     * /api/integrations/sleeper/players/sync
     */
    @PostMapping("/players/sync")
    public SleeperPlayerSyncResult
    syncPlayers() {

        return sleeperPlayerSyncService
                .syncPlayers();
    }


    /**
     * ===================================
     * Username → League 목록
     * ===================================
     *
     * 예:
     *
     * GET
     *
     * /api/integrations/sleeper
     * /users/USERNAME/leagues
     * ?season=2026
     */
    @GetMapping(
            "/users/{username}/leagues"
    )
    public List<SleeperLeaguePreview>
    getUserLeagues(

            @PathVariable
            String username,

            @RequestParam
            int season
    ) {

        return sleeperLeagueSyncService
                .getUserLeagues(

                        username,

                        season
                );
    }


    /**
     * ===================================
     * 선택한 League DB 동기화
     * ===================================
     *
     * POST
     *
     * /api/integrations/sleeper
     * /leagues/{leagueId}/sync
     */
    @PostMapping(
            "/leagues/{leagueId}/sync"
    )
    public SleeperLeagueSyncResult
    syncLeague(

            @PathVariable
            String leagueId,

            @RequestParam(
                    required = false
            )
            String username
    ) {

        return sleeperLeagueSyncService
                .syncLeague(

                        leagueId,

                        username
                );
    }
}