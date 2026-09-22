package com.taegun.fantasy.integration.playerids;

import com.taegun.fantasy.player.Player;
import com.taegun.fantasy.player.PlayerExternalId;
import com.taegun.fantasy.player.PlayerExternalIdRepository;
import com.taegun.fantasy.player.PlayerProvider;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Fantasy 서비스의 Player ID를
 * NFL GSIS ID와 연결합니다.
 *
 * 현재는 DynastyProcess의
 * db_playerids.csv 데이터를 사용합니다.
 */
@Service
public class PlayerIdSyncService {

    /**
     * DynastyProcess Player ID Database
     */
    private static final String PLAYER_ID_URL =
            "https://raw.githubusercontent.com/"
                    + "dynastyprocess/data/master/files/"
                    + "db_playerids.csv";


    private final PlayerExternalIdRepository
            playerExternalIdRepository;


    private final HttpClient httpClient;


    public PlayerIdSyncService(

            PlayerExternalIdRepository
                    playerExternalIdRepository
    ) {

        this.playerExternalIdRepository =
                playerExternalIdRepository;


        this.httpClient =
                HttpClient
                        .newBuilder()

                        .connectTimeout(
                                Duration.ofSeconds(10)
                        )

                        .build();
    }


    /**
     * Sleeper ID를 기준으로
     * GSIS ID를 우리 Player에 연결합니다.
     */
    @Transactional
    public PlayerIdSyncResult syncGsisIds() {

        try {

            /*
             * =========================
             * 1. CSV 다운로드
             * =========================
             */

            HttpRequest request =
                    HttpRequest
                            .newBuilder()

                            .uri(
                                    URI.create(
                                            PLAYER_ID_URL
                                    )
                            )

                            .timeout(
                                    Duration.ofSeconds(60)
                            )

                            .GET()

                            .build();


            HttpResponse<String> response =
                    httpClient.send(

                            request,

                            HttpResponse
                                    .BodyHandlers
                                    .ofString()
                    );


            if (
                    response.statusCode() < 200
                            ||
                            response.statusCode() >= 300
            ) {

                throw new RuntimeException(

                        "Player ID database request failed. status="
                                + response.statusCode()
                );
            }


            String csv =
                    response.body();


            /*
             * Windows / Linux 줄바꿈 모두 처리합니다.
             */
            String[] lines =
                    csv.split("\\R");


            if (
                    lines.length <= 1
            ) {

                throw new RuntimeException(
                        "Player ID CSV is empty"
                );
            }


            /*
             * =========================
             * 2. CSV 구조 확인
             * =========================
             *
             * 현재 CSV 시작 컬럼:
             *
             * 0 mfl_id
             * 1 sportradar_id
             * 2 fantasypros_id
             * 3 gsis_id
             * 4 pff_id
             * 5 sleeper_id
             *
             * 우리는 3번과 5번만 사용합니다.
             */

            String[] headers =
                    lines[0].split(",");


            if (
                    headers.length <= 5
                            ||
                            !"gsis_id".equals(
                                    headers[3]
                            )
                            ||
                            !"sleeper_id".equals(
                                    headers[5]
                            )
            ) {

                throw new RuntimeException(
                        "Unexpected Player ID CSV format"
                );
            }


            int sourceRows =
                    lines.length - 1;


            /*
             * =========================
             * 3. 기존 Sleeper 매핑 읽기
             * =========================
             */

            List<PlayerExternalId>
                    sleeperMappings =
                    playerExternalIdRepository
                            .findByProvider(
                                    PlayerProvider.SLEEPER
                            );


            /*
             * sleeperId → PlayerExternalId
             */
            Map<String, PlayerExternalId>
                    sleeperByExternalId =
                    new HashMap<>();


            for (
                    PlayerExternalId mapping
                    :
                    sleeperMappings
            ) {

                sleeperByExternalId.put(

                        mapping.getExternalId(),

                        mapping
                );
            }


            /*
             * =========================
             * 4. 기존 GSIS 매핑 읽기
             * =========================
             */

            List<PlayerExternalId>
                    gsisMappings =
                    playerExternalIdRepository
                            .findByProvider(
                                    PlayerProvider.GSIS
                            );


            /*
             * playerId → GSIS Mapping
             */
            Map<Long, PlayerExternalId>
                    gsisByPlayerId =
                    new HashMap<>();


            /*
             * gsisId → GSIS Mapping
             *
             * 같은 GSIS ID가 다른 선수에게
             * 중복 연결되는 것을 확인하기 위해 사용합니다.
             */
            Map<String, PlayerExternalId>
                    gsisByExternalId =
                    new HashMap<>();


            for (
                    PlayerExternalId mapping
                    :
                    gsisMappings
            ) {

                gsisByPlayerId.put(

                        mapping
                                .getPlayer()
                                .getId(),

                        mapping
                );


                gsisByExternalId.put(

                        mapping.getExternalId(),

                        mapping
                );
            }


            int usableRows = 0;

            int matchedPlayers = 0;

            int createdMappings = 0;

            int updatedMappings = 0;

            int alreadyCurrent = 0;

            int skippedNoSleeperMatch = 0;

            int conflicts = 0;


            /*
             * =========================
             * 5. CSV 행 처리
             * =========================
             */

            for (
                    int i = 1;
                    i < lines.length;
                    i++
            ) {

                String line =
                        lines[i];


                if (
                        line == null
                                ||
                                line.isBlank()
                ) {

                    continue;
                }


                /*
                 * 앞 6개 필드만 정확히 필요하므로
                 * 7개까지만 분리합니다.
                 *
                 * 뒤쪽 이름/학교 등에 쉼표가 있어도
                 * 우리가 필요한 앞쪽 ID에는 영향을
                 * 주지 않도록 합니다.
                 */
                String[] columns =
                        line.split(
                                ",",
                                7
                        );


                if (
                        columns.length < 6
                ) {

                    continue;
                }


                /*
                 * CSV 4번째 컬럼
                 */
                String gsisId =
                        normalizeId(
                                columns[3]
                        );


                /*
                 * CSV 6번째 컬럼
                 */
                String sleeperId =
                        normalizeId(
                                columns[5]
                        );


                /*
                 * 둘 중 하나라도 없으면
                 * 연결할 수 없습니다.
                 */
                if (
                        gsisId == null
                                ||
                                sleeperId == null
                ) {

                    continue;
                }


                usableRows++;


                /*
                 * =========================
                 * 6. Sleeper ID로
                 *    우리 Player 찾기
                 * =========================
                 */

                PlayerExternalId sleeperMapping =
                        sleeperByExternalId
                                .get(
                                        sleeperId
                                );


                /*
                 * 우리 DB에 없는 Sleeper 선수라면
                 * 이번에는 처리하지 않습니다.
                 */
                if (
                        sleeperMapping == null
                ) {

                    skippedNoSleeperMatch++;

                    continue;
                }


                Player player =
                        sleeperMapping
                                .getPlayer();


                matchedPlayers++;


                Long playerId =
                        player.getId();


                /*
                 * =========================
                 * 7. GSIS ID 중복 검사
                 * =========================
                 */

                PlayerExternalId sameGsisMapping =
                        gsisByExternalId
                                .get(
                                        gsisId
                                );


                /*
                 * 동일 GSIS ID가
                 * 다른 Player에 이미 연결돼 있다면
                 * 안전하게 건너뜁니다.
                 */
                if (
                        sameGsisMapping != null
                                &&
                                !sameGsisMapping
                                        .getPlayer()
                                        .getId()
                                        .equals(
                                                playerId
                                        )
                ) {

                    conflicts++;

                    continue;
                }


                /*
                 * 이 Player가 이미 GSIS 연결을
                 * 가지고 있는지 확인합니다.
                 */
                PlayerExternalId existingGsis =
                        gsisByPlayerId
                                .get(
                                        playerId
                                );


                /*
                 * =========================
                 * 8-A. GSIS 연결이 없는 경우
                 * =========================
                 */

                if (
                        existingGsis == null
                ) {

                    PlayerExternalId newMapping =
                            new PlayerExternalId(

                                    player,

                                    PlayerProvider.GSIS,

                                    gsisId
                            );


                    PlayerExternalId saved =
                            playerExternalIdRepository
                                    .save(
                                            newMapping
                                    );


                    gsisByPlayerId.put(

                            playerId,

                            saved
                    );


                    gsisByExternalId.put(

                            gsisId,

                            saved
                    );


                    createdMappings++;

                    continue;
                }


                /*
                 * =========================
                 * 8-B. 이미 동일한 GSIS
                 * =========================
                 */

                if (
                        existingGsis
                                .getExternalId()
                                .equals(
                                        gsisId
                                )
                ) {

                    alreadyCurrent++;

                    continue;
                }


                /*
                 * =========================
                 * 8-C. GSIS ID가 변경된 경우
                 * =========================
                 */

                String oldGsisId =
                        existingGsis
                                .getExternalId();


                existingGsis.setExternalId(
                        gsisId
                );


                PlayerExternalId saved =
                        playerExternalIdRepository
                                .save(
                                        existingGsis
                                );


                /*
                 * Map도 같이 갱신합니다.
                 */
                gsisByExternalId.remove(
                        oldGsisId
                );


                gsisByExternalId.put(

                        gsisId,

                        saved
                );


                updatedMappings++;
            }


            /*
             * =========================
             * 9. 결과 반환
             * =========================
             */

            return new PlayerIdSyncResult(

                    sourceRows,

                    usableRows,

                    matchedPlayers,

                    createdMappings,

                    updatedMappings,

                    alreadyCurrent,

                    skippedNoSleeperMatch,

                    conflicts
            );


        } catch (Exception e) {

            throw new RuntimeException(

                    "GSIS player ID synchronization failed",

                    e
            );
        }
    }


    /**
     * CSV의 ID 값을 정리합니다.
     *
     * NA
     * 빈 문자열
     *
     * 은 null로 처리합니다.
     */
    private String normalizeId(
            String value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        String normalized =
                value.trim();


        if (
                normalized.isEmpty()

                        ||

                        normalized.equalsIgnoreCase(
                                "NA"
                        )
        ) {

            return null;
        }


        return normalized;
    }
}