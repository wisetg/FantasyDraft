package com.taegun.fantasy.controller;

import com.taegun.fantasy.player.Player;
import com.taegun.fantasy.player.PlayerRepository;
import com.taegun.fantasy.player.PlayerStats;
import com.taegun.fantasy.player.PlayerStatsRepository;
import com.taegun.fantasy.player.PlayerStatsResponse;
import com.taegun.fantasy.player.ScoringFormat;

import com.taegun.fantasy.service.FantasyScoreService;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;


/**
 * 선수 경기 기록 API입니다.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerStatsController {

    private final PlayerRepository playerRepository;

    private final PlayerStatsRepository playerStatsRepository;

    private final FantasyScoreService fantasyScoreService;


    public PlayerStatsController(

            PlayerRepository playerRepository,

            PlayerStatsRepository playerStatsRepository,

            FantasyScoreService fantasyScoreService
    ) {

        this.playerRepository =
                playerRepository;

        this.playerStatsRepository =
                playerStatsRepository;

        this.fantasyScoreService =
                fantasyScoreService;
    }


    /**
     * 기존 경기 기록 조회
     *
     * GET
     *
     * /api/players/1/stats
     */
    @GetMapping("/{playerId}/stats")
    public List<PlayerStats> getStats(

            @PathVariable Long playerId
    ) {

        return playerStatsRepository
                .findByPlayerIdOrderBySeasonAscWeekAsc(
                        playerId
                );
    }


    /**
     * Scoring Format에 맞춰
     * Fantasy Points를 다시 계산해서 반환합니다.
     *
     * 예:
     *
     * /api/players/1/stats/scored
     * ?season=2026
     * &scoring=PPR
     */
    @GetMapping("/{playerId}/stats/scored")
    public List<PlayerStatsResponse> getScoredStats(

            @PathVariable Long playerId,

            @RequestParam(
                    required = false
            )
            Integer season,

            @RequestParam(
                    defaultValue = "PPR"
            )
            ScoringFormat scoring
    ) {


        List<PlayerStats> stats;


        /*
         * 시즌을 지정한 경우
         */
        if (season != null) {

            stats =
                    playerStatsRepository
                            .findByPlayerIdAndSeasonOrderByWeekAsc(
                                    playerId,
                                    season
                            );

        } else {

            /*
             * 시즌을 지정하지 않은 경우
             * 전체 기록
             */
            stats =
                    playerStatsRepository
                            .findByPlayerIdOrderBySeasonAscWeekAsc(
                                    playerId
                            );
        }


        /*
         * 각 경기의 Fantasy Points를
         * 선택한 Scoring Format으로
         * 다시 계산합니다.
         */
        return stats.stream()

                .map(statsRow -> {

                    double fantasyPoints =
                            fantasyScoreService
                                    .calculateScore(
                                            statsRow,
                                            scoring
                                    );


                    return PlayerStatsResponse
                            .from(
                                    statsRow,
                                    fantasyPoints
                            );
                })

                .toList();
    }


    /**
     * 경기 기록 추가
     *
     * POST
     *
     * /api/players/{playerId}/stats
     *
     * 저장 기본값은 PPR입니다.
     */
    @PostMapping("/{playerId}/stats")
    public PlayerStats createStats(

            @PathVariable Long playerId,

            @RequestBody PlayerStats request
    ) {


        Player player =
                playerRepository
                        .findById(playerId)
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Player not found"
                                )

                        );


        /*
         * DB에 저장하는 기본 fantasyPoints는
         * PPR을 사용합니다.
         */
        double fantasyPoints =
                fantasyScoreService
                        .calculateScore(

                                request,

                                ScoringFormat.PPR
                        );


        PlayerStats stats =
                new PlayerStats(

                        player,

                        request.getSeason(),

                        request.getWeek(),

                        fantasyPoints,


                        request.getPassingYards(),

                        request.getPassingTouchdowns(),

                        request.getInterceptions(),


                        request.getRushingYards(),

                        request.getRushingTouchdowns(),


                        request.getReceptions(),

                        request.getReceivingYards(),

                        request.getReceivingTouchdowns(),


                        request.getFumblesLost()
                );


        return playerStatsRepository
                .save(stats);
    }
}