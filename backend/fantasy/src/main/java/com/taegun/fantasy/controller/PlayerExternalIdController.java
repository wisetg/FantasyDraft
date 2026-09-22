package com.taegun.fantasy.controller;

import com.taegun.fantasy.player.*;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;


/**
 * Player와 외부 서비스 ID를
 * 연결하는 API입니다.
 */
@RestController
@RequestMapping(
        "/api/players/{playerId}/external-ids"
)
public class PlayerExternalIdController {

    private final PlayerRepository playerRepository;

    private final PlayerExternalIdRepository
            playerExternalIdRepository;


    public PlayerExternalIdController(

            PlayerRepository playerRepository,

            PlayerExternalIdRepository
                    playerExternalIdRepository
    ) {

        this.playerRepository =
                playerRepository;

        this.playerExternalIdRepository =
                playerExternalIdRepository;
    }


    /**
     * 특정 선수의 외부 ID 목록
     *
     * GET
     *
     * /api/players/1/external-ids
     */
    @GetMapping
    public List<PlayerExternalIdResponse>
    getExternalIds(

            @PathVariable Long playerId
    ) {

        /*
         * 존재하지 않는 선수라면
         * 404를 반환합니다.
         */
        if (
                !playerRepository
                        .existsById(playerId)
        ) {

            throw new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Player not found"
            );
        }


        return playerExternalIdRepository
                .findByPlayer_IdOrderByProviderAsc(
                        playerId
                )
                .stream()
                .map(
                        PlayerExternalIdResponse::from
                )
                .toList();
    }


    /**
     * 외부 ID 등록 또는 수정
     *
     * PUT
     *
     * /api/players/1/external-ids/SLEEPER
     *
     * Body:
     *
     * {
     *   "externalId": "..."
     * }
     */
    @PutMapping("/{provider}")
    public PlayerExternalIdResponse
    saveExternalId(

            @PathVariable Long playerId,

            @PathVariable
            PlayerProvider provider,

            @RequestBody
            PlayerExternalIdRequest request
    ) {


        /*
         * Player 확인
         */
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
         * External ID 검증
         */
        if (
                request.getExternalId()
                        == null

                        ||

                        request.getExternalId()
                                .isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "External ID is required"
            );
        }


        String externalId =
                request
                        .getExternalId()
                        .trim();


        /*
         * 같은 Provider + External ID가
         * 다른 Player에게 이미 연결돼 있는지 확인합니다.
         */
        playerExternalIdRepository
                .findByProviderAndExternalId(

                        provider,

                        externalId
                )
                .ifPresent(existing -> {

                    Long existingPlayerId =
                            existing
                                    .getPlayer()
                                    .getId();


                    if (
                            !existingPlayerId
                                    .equals(playerId)
                    ) {

                        throw new ResponseStatusException(

                                HttpStatus.CONFLICT,

                                "External ID is already linked to another player"
                        );
                    }

                });


        /*
         * 해당 선수의 Provider 연결이
         * 이미 있으면 수정하고,
         *
         * 없으면 새로 만듭니다.
         */
        PlayerExternalId mapping =
                playerExternalIdRepository
                        .findByPlayer_IdAndProvider(

                                playerId,

                                provider
                        )
                        .orElseGet(() ->

                                new PlayerExternalId(

                                        player,

                                        provider,

                                        externalId
                                )

                        );


        mapping.setExternalId(
                externalId
        );


        PlayerExternalId saved =
                playerExternalIdRepository
                        .save(mapping);


        return PlayerExternalIdResponse
                .from(saved);
    }
}