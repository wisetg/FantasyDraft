package com.taegun.fantasy.player;

/**
 * Player External ID API 응답 DTO입니다.
 */
public class PlayerExternalIdResponse {

    private final Long id;

    private final Long playerId;

    private final PlayerProvider provider;

    private final String externalId;


    public PlayerExternalIdResponse(

            Long id,

            Long playerId,

            PlayerProvider provider,

            String externalId
    ) {

        this.id =
                id;

        this.playerId =
                playerId;

        this.provider =
                provider;

        this.externalId =
                externalId;
    }


    /**
     * Entity → Response DTO
     */
    public static PlayerExternalIdResponse from(
            PlayerExternalId externalId
    ) {

        return new PlayerExternalIdResponse(

                externalId.getId(),

                externalId
                        .getPlayer()
                        .getId(),

                externalId.getProvider(),

                externalId.getExternalId()
        );
    }


    public Long getId() {

        return id;
    }


    public Long getPlayerId() {

        return playerId;
    }


    public PlayerProvider getProvider() {

        return provider;
    }


    public String getExternalId() {

        return externalId;
    }
}