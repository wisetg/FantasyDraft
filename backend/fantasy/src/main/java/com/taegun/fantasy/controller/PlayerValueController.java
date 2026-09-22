package com.taegun.fantasy.controller;

import com.taegun.fantasy.player.PlayerValue;
import com.taegun.fantasy.player.ScoringFormat;

import com.taegun.fantasy.service.PlayerValueService;

import org.springframework.web.bind.annotation.*;


/**
 * 선수 Trade Value API입니다.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerValueController {

    private final PlayerValueService playerValueService;


    public PlayerValueController(
            PlayerValueService playerValueService
    ) {

        this.playerValueService =
                playerValueService;
    }


    /**
     * 예:
     *
     * /api/players/1/value
     *
     * /api/players/1/value
     * ?season=2026
     * &scoring=HALF_PPR
     */
    @GetMapping("/{id}/value")
    public PlayerValue getPlayerValue(

            @PathVariable Long id,

            @RequestParam(
                    required = false
            )
            Integer season,

            @RequestParam(
                    defaultValue = "PPR"
            )
            ScoringFormat scoring
    ) {


        if (season == null) {

            return playerValueService
                    .calculatePlayerValue(

                            id,

                            scoring
                    );
        }


        return playerValueService
                .calculatePlayerValue(

                        id,

                        season,

                        scoring
                );
    }
}