package com.taegun.fantasy.player;

/**
 * 외부 Player ID 등록 요청입니다.
 */
public class PlayerExternalIdRequest {

    private String externalId;


    public PlayerExternalIdRequest() {
    }


    public String getExternalId() {

        return externalId;
    }


    public void setExternalId(
            String externalId
    ) {

        this.externalId =
                externalId;
    }
}