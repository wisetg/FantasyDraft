package com.taegun.fantasy.controller;

import com.taegun.fantasy.player.Player;
import com.taegun.fantasy.player.PlayerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 선수 기본 정보 API입니다.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerRepository playerRepository;


    public PlayerController(
            PlayerRepository playerRepository
    ) {

        this.playerRepository =
                playerRepository;
    }


    /**
     * 선수 목록 조회 / 검색 / 필터
     *
     * GET /api/players
     *
     * GET /api/players?search=justin
     *
     * GET /api/players?position=WR
     *
     * GET /api/players?team=MIN
     */
    @GetMapping
    public List<Player> getPlayers(

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String search,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String position,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String team
    ) {

        return playerRepository
                .searchPlayers(

                        normalize(search),

                        normalize(position),

                        normalize(team)
                );
    }


    /**
     * 포지션 목록
     *
     * GET /api/players/positions
     */
    @GetMapping("/positions")
    public List<String> getPositions() {

        return playerRepository
                .findDistinctPositions();
    }


    /**
     * 팀 목록
     *
     * GET /api/players/teams
     */
    @GetMapping("/teams")
    public List<String> getTeams() {

        return playerRepository
                .findDistinctTeams();
    }


    /**
     * 특정 선수 조회
     *
     * GET /api/players/{id}
     */
    @GetMapping("/{id}")
    public Player getPlayer(
            @PathVariable Long id
    ) {

        return playerRepository
                .findById(id)
                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "Player not found"
                        )

                );
    }


    /**
     * 새로운 선수 저장
     *
     * POST /api/players
     */
    @PostMapping
    public Player createPlayer(

            @RequestBody Player player
    ) {

        return playerRepository
                .save(player);
    }


    /**
     * 앞뒤 공백 제거
     *
     * 검색 조건이 없으면
     * 빈 문자열을 유지합니다.
     */
    private String normalize(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value.trim();
    }
}