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

        let message =
            `API 요청 실패: ${response.status}`;


        try {

            const data =
                await response.json();


            if (data.message) {

                message =
                    data.message;

            }

        } catch {

            // JSON 형태가 아닌 오류라면
            // 기본 메시지를 사용합니다.

        }


        throw new Error(
            message
        );

    }


    return response.json();
}


/**
 * Sleeper Username으로
 * 특정 시즌의 League 목록을 가져옵니다.
 */
export async function getSleeperLeagues(

    username,

    season

) {

    const encodedUsername =
        encodeURIComponent(
            username
        );


    return requestJson(

        `${API_BASE_URL}/integrations/sleeper/users/${encodedUsername}/leagues?season=${season}`

    );
}


/**
 * 선택한 Sleeper League를
 * 우리 DB와 동기화합니다.
 *
 * username을 같이 보내서
 * 실제 "내 팀"을 Backend가 찾도록 합니다.
 */
export async function syncSleeperLeague(

    externalLeagueId,

    username

) {

    const params =
        new URLSearchParams();


    if (
        username
        &&
        username.trim()
    ) {

        params.set(
            "username",
            username.trim()
        );

    }


    const queryString =
        params.toString();


    const url =
        queryString
            ? `${API_BASE_URL}/integrations/sleeper/leagues/${externalLeagueId}/sync?${queryString}`
            : `${API_BASE_URL}/integrations/sleeper/leagues/${externalLeagueId}/sync`;


    return requestJson(

        url,

        {
            method: "POST",
        }

    );
}


/**
 * 우리 DB에 저장된
 * Fantasy League 상세 정보를 가져옵니다.
 *
 * Team + Roster 포함
 */
export async function getFantasyLeague(
    fantasyLeagueId
) {

    return requestJson(

        `${API_BASE_URL}/leagues/${fantasyLeagueId}`

    );
}
export async function getScoringCompatibility(
    fantasyLeagueId
) {

    return requestJson(
        `${API_BASE_URL}/leagues/${fantasyLeagueId}/scoring-compatibility`
    );
}
