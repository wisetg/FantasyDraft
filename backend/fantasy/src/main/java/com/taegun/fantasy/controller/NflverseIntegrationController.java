package com.taegun.fantasy.controller;

import com.taegun.fantasy.integration.nflverse.NflverseDefenseStatsSyncService;
import com.taegun.fantasy.integration.nflverse.NflverseDefenseSyncResult;
import com.taegun.fantasy.integration.nflverse.NflverseStatsSyncResult;
import com.taegun.fantasy.integration.nflverse.NflverseStatsSyncService;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;


/**
 * nflverse 데이터 동기화 API입니다.
 */
@RestController
@RequestMapping(
        "/api/integrations/nflverse"
)
public class NflverseIntegrationController {

    private final NflverseStatsSyncService
            nflverseStatsSyncService;

    private final NflverseDefenseStatsSyncService
            nflverseDefenseStatsSyncService;


    public NflverseIntegrationController(
            NflverseStatsSyncService nflverseStatsSyncService,
            NflverseDefenseStatsSyncService nflverseDefenseStatsSyncService
    ) {

        this.nflverseStatsSyncService =
                nflverseStatsSyncService;

        this.nflverseDefenseStatsSyncService =
                nflverseDefenseStatsSyncService;
    }


    /**
     * QB / RB / WR / TE / Kicker
     *
     * POST
     *
     * /api/integrations/nflverse/stats/sync?season=2026
     */
    @PostMapping(
            "/stats/sync"
    )
    public NflverseStatsSyncResult syncStats(
            @RequestParam int season
    ) {

        validateSeason(
                season
        );


        return nflverseStatsSyncService
                .syncSeason(
                        season
                );
    }


    /**
     * Team Defense
     *
     * POST
     *
     * /api/integrations/nflverse/defense/sync?season=2026
     */
    @PostMapping(
            "/defense/sync"
    )
    public NflverseDefenseSyncResult syncDefense(
            @RequestParam int season
    ) {

        validateSeason(
                season
        );


        return nflverseDefenseStatsSyncService
                .syncSeason(
                        season
                );
    }


    /**
     * 잘못된 시즌 입력 방지
     */
    private void validateSeason(
            int season
    ) {

        if (
                season < 1999
                        ||
                        season > 2100
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid NFL season"
            );
        }
    }
}