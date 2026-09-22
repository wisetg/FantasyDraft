package com.taegun.fantasy.integration.sleeper;

import com.taegun.fantasy.player.Player;
import com.taegun.fantasy.player.PlayerExternalId;
import com.taegun.fantasy.player.PlayerExternalIdRepository;
import com.taegun.fantasy.player.PlayerProvider;
import com.taegun.fantasy.player.PlayerRepository;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.Duration;

import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Sleeper의 NFL 선수 데이터를
 * 우리 PostgreSQL과 동기화합니다.
 */
@Service
public class SleeperPlayerSyncService {

    /**
     * 현재 프로젝트에서 사용할
     * Fantasy Football 주요 포지션입니다.
     */
    private static final Set<String> SUPPORTED_POSITIONS =
            Set.of(
                    "QB",
                    "RB",
                    "WR",
                    "TE",
                    "K",
                    "DEF"
            );


    /**
     * Sleeper의 Active NFL Player API
     */
    private static final String SLEEPER_PLAYERS_URL =
            "https://api.sleeper.app/v1/players/nfl?active=true";


    private final PlayerRepository playerRepository;

    private final PlayerExternalIdRepository playerExternalIdRepository;

    private final JsonMapper jsonMapper;

    private final HttpClient httpClient;


    public SleeperPlayerSyncService(

            PlayerRepository playerRepository,

            PlayerExternalIdRepository playerExternalIdRepository,

            JsonMapper jsonMapper
    ) {

        this.playerRepository =
                playerRepository;

        this.playerExternalIdRepository =
                playerExternalIdRepository;

        this.jsonMapper =
                jsonMapper;


        this.httpClient =
                HttpClient
                        .newBuilder()

                        .connectTimeout(
                                Duration.ofSeconds(10)
                        )

                        .build();
    }


    /**
     * Sleeper 선수 데이터를 가져와
     * 우리 DB에 저장합니다.
     */
    public SleeperPlayerSyncResult syncPlayers() {

        try {

            /*
             * =========================
             * 1. Sleeper API 요청
             * =========================
             */

            HttpRequest request =
                    HttpRequest
                            .newBuilder()

                            .uri(
                                    URI.create(
                                            SLEEPER_PLAYERS_URL
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


            /*
             * HTTP 200번대가 아니면
             * 동기화를 중단합니다.
             */
            if (
                    response.statusCode() < 200
                            ||
                            response.statusCode() >= 300
            ) {

                throw new RuntimeException(

                        "Sleeper API request failed. status="
                                + response.statusCode()
                );
            }


            /*
             * =========================
             * 2. JSON 변환
             * =========================
             */

            JsonNode root =
                    jsonMapper.readTree(
                            response.body()
                    );


            if (!root.isObject()) {

                throw new RuntimeException(
                        "Unexpected Sleeper player response"
                );
            }


            int receivedPlayers =
                    root.size();


            int eligiblePlayers = 0;

            int createdPlayers = 0;

            int updatedPlayers = 0;

            int linkedExistingPlayers = 0;

            int skippedPlayers = 0;


            /*
             * Sleeper의 선수 데이터는:
             *
             * {
             *   "선수ID": {
             *       선수 정보...
             *   },
             *
             *   "선수ID": {
             *       선수 정보...
             *   }
             * }
             *
             * 구조입니다.
             *
             * Jackson 3에서는
             * fields() 대신 properties()를 사용합니다.
             */
            for (
                    Map.Entry<String, JsonNode> entry
                    :
                    root.properties()
            ) {

                /*
                 * Sleeper Player ID
                 */
                String sleeperPlayerId =
                        entry.getKey();


                JsonNode data =
                        entry.getValue();


                /*
                 * Position
                 */
                String position =
                        getText(
                                data,
                                "position"
                        );


                /*
                 * 현재 우리가 지원하는
                 * 포지션이 아니라면 건너뜁니다.
                 */
                if (
                        position == null
                                ||
                                !SUPPORTED_POSITIONS
                                        .contains(
                                                position.toUpperCase()
                                        )
                ) {

                    skippedPlayers++;

                    continue;
                }


                /*
                 * 선수 이름
                 */
                String name =
                        getPlayerName(
                                data
                        );

                /*
                 * Team Defense는 일반 선수와 다르게
                 * full_name이 없는 경우가 있으므로
                 * Team Code를 이름으로 사용합니다.
                 */
                if (
                        (name == null || name.isBlank())
                                &&
                                "DEF".equalsIgnoreCase(
                                        position
                                )
                ) {

                    String defenseTeam =
                            getText(
                                    data,
                                    "team"
                            );


                    if (
                            defenseTeam == null
                                    ||
                                    defenseTeam.isBlank()
                    ) {

                        defenseTeam =
                                sleeperPlayerId;
                    }


                    name =
                            defenseTeam
                                    + " Defense";
                }


                if (
                        name == null
                                ||
                                name.isBlank()
                ) {

                    skippedPlayers++;

                    continue;
                }


                /*
                 * NFL Team
                 */
                String team =
                        getText(
                                data,
                                "team"
                        );


                /*
                 * 현재 소속 팀이 없는 선수는
                 * FA(Free Agent)로 저장합니다.
                 */
                if (
                        team == null
                                ||
                                team.isBlank()
                ) {

                    if (
                            "DEF".equalsIgnoreCase(
                                    position
                            )
                    ) {

                        team =
                                sleeperPlayerId;

                    } else {

                        team =
                                "FA";
                    }
                }


                position =
                        position.toUpperCase();


                team =
                        team.toUpperCase();


                eligiblePlayers++;


                /*
                 * =========================
                 * 3. Sleeper External ID가
                 *    이미 존재하는지 확인
                 * =========================
                 */

                Optional<PlayerExternalId> existingMapping =
                        playerExternalIdRepository
                                .findByProviderAndExternalId(

                                        PlayerProvider.SLEEPER,

                                        sleeperPlayerId
                                );


                /*
                 * 이미 연결되어 있는 선수라면
                 * 선수 기본 정보만 갱신합니다.
                 */
                if (
                        existingMapping.isPresent()
                ) {

                    Player player =
                            existingMapping
                                    .get()
                                    .getPlayer();


                    player.updateBasicInfo(

                            name,

                            position,

                            team
                    );


                    playerRepository.save(
                            player
                    );


                    updatedPlayers++;

                    continue;
                }


                /*
                 * =========================
                 * 4. 기존 내부 Player 찾기
                 * =========================
                 *
                 * 이름 + Position + Team이 같으면
                 * 기존 Player와 연결합니다.
                 *
                 * 기존 Justin Jefferson / Bijan
                 * 테스트 데이터가 여기에 해당합니다.
                 */

                Optional<Player> existingPlayer =
                        playerRepository
                                .findFirstByNameIgnoreCaseAndPositionIgnoreCaseAndTeamIgnoreCase(

                                        name,

                                        position,

                                        team
                                );


                Player player;


                if (
                        existingPlayer.isPresent()
                ) {

                    player =
                            existingPlayer.get();


                    player.updateBasicInfo(

                            name,

                            position,

                            team
                    );


                    playerRepository.save(
                            player
                    );


                    linkedExistingPlayers++;

                } else {

                    /*
                     * 우리 DB에 없는 선수라면
                     * 새 Player를 생성합니다.
                     */
                    player =
                            new Player(

                                    name,

                                    position,

                                    team
                            );


                    player =
                            playerRepository.save(
                                    player
                            );


                    createdPlayers++;
                }


                /*
                 * =========================
                 * 5. Sleeper External ID 연결
                 * =========================
                 */

                Optional<PlayerExternalId> playerSleeperMapping =
                        playerExternalIdRepository
                                .findByPlayer_IdAndProvider(

                                        player.getId(),

                                        PlayerProvider.SLEEPER
                                );


                PlayerExternalId mapping;


                /*
                 * 이미 Sleeper ID가 있으면
                 *
                 * test-sleeper-jefferson
                 *
                 * 같은 테스트 ID를
                 * 실제 Sleeper ID로 교체합니다.
                 */
                if (
                        playerSleeperMapping.isPresent()
                ) {

                    mapping =
                            playerSleeperMapping.get();


                    mapping.setExternalId(
                            sleeperPlayerId
                    );

                } else {

                    /*
                     * Sleeper 연결 자체가 없으면
                     * 새로 생성합니다.
                     */
                    mapping =
                            new PlayerExternalId(

                                    player,

                                    PlayerProvider.SLEEPER,

                                    sleeperPlayerId
                            );
                }


                playerExternalIdRepository.save(
                        mapping
                );
            }


            /*
             * =========================
             * 6. 결과 반환
             * =========================
             */

            return new SleeperPlayerSyncResult(

                    receivedPlayers,

                    eligiblePlayers,

                    createdPlayers,

                    updatedPlayers,

                    linkedExistingPlayers,

                    skippedPlayers
            );


        } catch (Exception e) {

            throw new RuntimeException(

                    "Sleeper player synchronization failed",

                    e
            );
        }
    }


    /**
     * JSON 문자열 값을
     * 안전하게 가져옵니다.
     */
    private String getText(

            JsonNode node,

            String fieldName
    ) {

        JsonNode field =
                node.get(
                        fieldName
                );


        if (
                field == null
                        ||
                        field.isNull()
        ) {

            return null;
        }


        /*
         * Jackson 3에서는
         * asString()을 사용합니다.
         */
        String value =
                field.asString();


        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return null;
        }


        return value.trim();
    }


    /**
     * Sleeper 선수 이름을 가져옵니다.
     */
    private String getPlayerName(
            JsonNode data
    ) {

        /*
         * full_name이 존재하면
         * 우선 사용합니다.
         */
        String fullName =
                getText(
                        data,
                        "full_name"
                );


        if (
                fullName != null
        ) {

            return fullName;
        }


        /*
         * full_name이 없다면
         *
         * first_name + last_name
         *
         * 으로 이름을 만듭니다.
         */
        String firstName =
                getText(
                        data,
                        "first_name"
                );


        String lastName =
                getText(
                        data,
                        "last_name"
                );


        StringBuilder name =
                new StringBuilder();


        if (
                firstName != null
        ) {

            name.append(
                    firstName
            );
        }


        if (
                lastName != null
        ) {

            if (
                    name.length() > 0
            ) {

                name.append(" ");
            }


            name.append(
                    lastName
            );
        }


        String result =
                name
                        .toString()
                        .trim();


        return result.isBlank()
                ? null
                : result;
    }
}