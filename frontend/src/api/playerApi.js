const API_BASE_URL =
    "/api";


async function requestJson(
    url,
    options = {}
) {

    const response =
        await fetch(
            url,
            options
        );


    if (!response.ok) {

        throw new Error(
            `API 요청 실패: ${response.status}`
        );

    }


    return response.json();
}


/**
 * 전체 선수
 */
export async function getPlayers() {

    return requestJson(
        `${API_BASE_URL}/players`
    );

}


/**
 * 특정 선수
 */
export async function getPlayerById(
    playerId
) {

    return requestJson(
        `${API_BASE_URL}/players/${playerId}`
    );

}


/**
 * 기존 Raw Stats
 *
 * 시즌 목록을 확인하기 위해 사용합니다.
 */
export async function getPlayerStats(
    playerId
) {

    const response =
        await fetch(
            `${API_BASE_URL}/players/${playerId}/stats`
        );


    if (
        response.status === 404
    ) {

        return [];

    }


    if (!response.ok) {

        throw new Error(
            "선수 경기 기록을 불러오는 데 실패했습니다."
        );

    }


    return response.json();
}


/**
 * 선택한 시즌 +
 * Scoring Format 기준 경기 기록
 */
export async function getPlayerScoredStats(

    playerId,

    season,

    scoring = "PPR"
) {

    const params =
        new URLSearchParams();


    if (
        season !== null
        &&
        season !== undefined
    ) {

        params.set(
            "season",
            season
        );

    }


    params.set(
        "scoring",
        scoring
    );


    const response =
        await fetch(

            `${API_BASE_URL}/players/${playerId}/stats/scored?${params.toString()}`

        );


    if (
        response.status === 404
    ) {

        return [];

    }


    if (!response.ok) {

        throw new Error(
            "계산된 경기 기록을 불러오지 못했습니다."
        );

    }


    return response.json();
}


/**
 * Fantasy Analysis
 */
export async function getPlayerAnalysis(

    playerId,

    season = null,

    scoring = "PPR"
) {

    const params =
        new URLSearchParams();


    if (season !== null) {

        params.set(
            "season",
            season
        );

    }


    params.set(
        "scoring",
        scoring
    );


    const response =
        await fetch(

            `${API_BASE_URL}/players/${playerId}/analysis?${params.toString()}`

        );


    if (
        response.status === 404
    ) {

        return null;

    }


    if (!response.ok) {

        throw new Error(
            "선수 분석 결과를 불러오지 못했습니다."
        );

    }


    return response.json();
}


/**
 * Trade Value
 */
export async function getPlayerValue(

    playerId,

    season = null,

    scoring = "PPR"
) {

    const params =
        new URLSearchParams();


    if (season !== null) {

        params.set(
            "season",
            season
        );

    }


    params.set(
        "scoring",
        scoring
    );


    const response =
        await fetch(

            `${API_BASE_URL}/players/${playerId}/value?${params.toString()}`

        );


    if (
        response.status === 404
    ) {

        return null;

    }


    if (!response.ok) {

        throw new Error(
            "선수 가치 정보를 불러오지 못했습니다."
        );

    }


    return response.json();
}
