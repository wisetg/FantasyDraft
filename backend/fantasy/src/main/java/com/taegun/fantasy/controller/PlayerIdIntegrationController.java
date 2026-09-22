package com.taegun.fantasy.controller;

import com.taegun.fantasy.integration.playerids.PlayerIdSyncResult;
import com.taegun.fantasy.integration.playerids.PlayerIdSyncService;

import org.springframework.web.bind.annotation.*;

/**
 * 외부 Player ID 동기화 API입니다.
 */
@RestController
@RequestMapping(
        "/api/integrations/player-ids"
)
public class PlayerIdIntegrationController {

    private final PlayerIdSyncService
            playerIdSyncService;


    public PlayerIdIntegrationController(

            PlayerIdSyncService
                    playerIdSyncService
    ) {

        this.playerIdSyncService =
                playerIdSyncService;
    }


    /**
     * Sleeper ID를 기준으로
     * GSIS ID를 연결합니다.
     *
     * POST
     *
     * /api/integrations/player-ids/gsis/sync
     */
    @PostMapping("/gsis/sync")
    public PlayerIdSyncResult
    syncGsisIds() {

        return playerIdSyncService
                .syncGsisIds();
    }
}