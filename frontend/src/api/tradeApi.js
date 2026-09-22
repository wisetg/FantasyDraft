const API_BASE_URL =
    "/api";


export async function simulateTrade(

    teamAPlayerIds,

    teamBPlayerIds,

    scoringFormat = "PPR",

    season = null,

    fantasyLeagueId = null

) {

    const response =
        await fetch(

            `${API_BASE_URL}/trades/simulate`,

            {

                method: "POST",


                headers: {

                    "Content-Type":
                        "application/json",

                },


                body: JSON.stringify({

                    teamAPlayerIds,

                    teamBPlayerIds,

                    scoringFormat,

                    season,

                    fantasyLeagueId,

                }),

            }

        );


    if (!response.ok) {

        let message =
            "트레이드 분석 중 오류가 발생했습니다.";


        try {

            const errorData =
                await response.json();


            if (
                errorData.message
            ) {

                message =
                    errorData.message;

            }

        } catch {

            // 기본 메시지 사용

        }


        throw new Error(
            message
        );

    }


    return response.json();
}
