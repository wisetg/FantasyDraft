package com.taegun.fantasy.controller;

import com.taegun.fantasy.player.PlayerAnalysis;
import com.taegun.fantasy.player.ScoringFormat;

import com.taegun.fantasy.service.PlayerAnalysisService;

import org.springframework.web.bind.annotation.*;


/**
 * 선수 분석 API입니다.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerAnalysisController {

    private final PlayerAnalysisService playerAnalysisService;


    public PlayerAnalysisController(
            PlayerAnalysisService playerAnalysisService
    ) {

        this.playerAnalysisService =
                playerAnalysisService;
    }


    /**
     * 예:
     *
     * /api/players/1/analysis
     *
     * /api/players/1/analysis
     * ?season=2026
     * &scoring=PPR
     */
    @GetMapping("/{id}/analysis")
    public PlayerAnalysis analyzePlayer(

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

            return playerAnalysisService
                    .analyzePlayer(

                            id,

                            scoring
                    );
        }


        return playerAnalysisService
                .analyzePlayer(

                        id,

                        season,

                        scoring
                );
    }
}