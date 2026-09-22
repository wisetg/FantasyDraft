package com.taegun.fantasy.integration.sleeper;

import com.taegun.fantasy.league.FantasyLeague;
import com.taegun.fantasy.league.FantasyLeagueRepository;
import com.taegun.fantasy.league.FantasyPlatform;
import com.taegun.fantasy.league.FantasyRosterPlayer;
import com.taegun.fantasy.league.FantasyRosterPlayerRepository;
import com.taegun.fantasy.league.FantasyTeam;
import com.taegun.fantasy.league.FantasyTeamRepository;
import com.taegun.fantasy.league.RosterPlayerStatus;

import com.taegun.fantasy.player.Player;
import com.taegun.fantasy.player.PlayerExternalId;
import com.taegun.fantasy.player.PlayerExternalIdRepository;
import com.taegun.fantasy.player.PlayerProvider;
import com.taegun.fantasy.player.ScoringFormat;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;

import java.time.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Sleeper User / League / Roster 동기화 Service입니다.
 */
@Service
public class SleeperLeagueSyncService {

    private static final String SLEEPER_API =
            "https://api.sleeper.app/v1";


    private final FantasyLeagueRepository
            fantasyLeagueRepository;

    private final FantasyTeamRepository
            fantasyTeamRepository;

    private final FantasyRosterPlayerRepository
            fantasyRosterPlayerRepository;

    private final PlayerExternalIdRepository
            playerExternalIdRepository;

    private final JsonMapper
            jsonMapper;

    private final HttpClient
            httpClient;


    public SleeperLeagueSyncService(

            FantasyLeagueRepository fantasyLeagueRepository,

            FantasyTeamRepository fantasyTeamRepository,

            FantasyRosterPlayerRepository
                    fantasyRosterPlayerRepository,

            PlayerExternalIdRepository
                    playerExternalIdRepository,

            JsonMapper jsonMapper
    ) {

        this.fantasyLeagueRepository =
                fantasyLeagueRepository;

        this.fantasyTeamRepository =
                fantasyTeamRepository;

        this.fantasyRosterPlayerRepository =
                fantasyRosterPlayerRepository;

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
     * Username → 시즌 League 목록
     */
    public List<SleeperLeaguePreview>
    getUserLeagues(

            String username,

            int season
    ) {

        String userId =
                getSleeperUserId(
                        username
                );


        JsonNode leagues =
                getJson(

                        SLEEPER_API
                                + "/user/"
                                + encodePath(userId)
                                + "/leagues/nfl/"
                                + season
                );


        if (
                !leagues.isArray()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_GATEWAY,

                    "Unexpected Sleeper league response"
            );
        }


        List<SleeperLeaguePreview> result =
                new ArrayList<>();


        for (
                int i = 0;
                i < leagues.size();
                i++
        ) {

            JsonNode league =
                    leagues.get(i);


            String leagueId =
                    getText(
                            league,
                            "league_id"
                    );


            if (
                    leagueId == null
            ) {

                continue;
            }


            String name =
                    getText(
                            league,
                            "name"
                    );


            if (
                    name == null
            ) {

                name =
                        "Sleeper League "
                                + leagueId;
            }


            int leagueSeason =
                    getInt(
                            league,
                            "season",
                            season
                    );


            int totalTeams =
                    getInt(
                            league,
                            "total_rosters",
                            0
                    );


            String status =
                    getText(
                            league,
                            "status"
                    );


            JsonNode scoringSettings =
                    league.get(
                            "scoring_settings"
                    );


            double receptionPoints =
                    getDouble(
                            scoringSettings,
                            "rec",
                            0
                    );


            ScoringFormat scoringFormat =
                    detectScoringFormat(
                            receptionPoints
                    );


            result.add(

                    new SleeperLeaguePreview(

                            leagueId,

                            name,

                            leagueSeason,

                            totalTeams,

                            status,

                            scoringFormat,

                            receptionPoints
                    )
            );
        }


        return result;
    }


    /**
     * Sleeper League 전체 동기화
     */
    @Transactional
    public SleeperLeagueSyncResult
    syncLeague(

            String sleeperLeagueId,

            String username
    ) {

        if (
                sleeperLeagueId == null
                        ||
                        sleeperLeagueId.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Sleeper league ID is required"
            );
        }


        String leagueId =
                sleeperLeagueId.trim();


        /*
         * Username → Sleeper User ID
         *
         * 이것으로 실제 내 Team을 찾습니다.
         */
        String currentUserId =
                null;


        if (
                username != null
                        &&
                        !username.isBlank()
        ) {

            currentUserId =
                    getSleeperUserId(
                            username
                    );
        }


        /*
         * =========================
         * Sleeper 데이터
         * =========================
         */

        JsonNode leagueJson =
                getJson(

                        SLEEPER_API
                                + "/league/"
                                + encodePath(
                                leagueId
                        )
                );


        JsonNode usersJson =
                getJson(

                        SLEEPER_API
                                + "/league/"
                                + encodePath(
                                leagueId
                        )
                                + "/users"
                );


        JsonNode rostersJson =
                getJson(

                        SLEEPER_API
                                + "/league/"
                                + encodePath(
                                leagueId
                        )
                                + "/rosters"
                );


        if (
                !leagueJson.isObject()
                        ||
                        !usersJson.isArray()
                        ||
                        !rostersJson.isArray()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_GATEWAY,

                    "Unexpected Sleeper league data"
            );
        }


        /*
         * =========================
         * Roster Position 설정
         * =========================
         */

        List<String> starterSlots =
                readStarterSlots(
                        leagueJson
                );


        /*
         * =========================
         * League 정보
         * =========================
         */

        String leagueName =
                getText(
                        leagueJson,
                        "name"
                );


        if (
                leagueName == null
        ) {

            leagueName =
                    "Sleeper League "
                            + leagueId;
        }


        int season =
                getInt(
                        leagueJson,
                        "season",
                        0
                );


        int totalTeams =
                getInt(
                        leagueJson,
                        "total_rosters",
                        rostersJson.size()
                );


        String status =
                getText(
                        leagueJson,
                        "status"
                );


        JsonNode scoringSettings =
                leagueJson.get(
                        "scoring_settings"
                );


        double receptionPoints =
                getDouble(
                        scoringSettings,
                        "rec",
                        0
                );


        ScoringFormat scoringFormat =
                detectScoringFormat(
                        receptionPoints
                );


        String scoringSettingsJson =
                toJson(
                        scoringSettings
                );


        /*
         * =========================
         * FantasyLeague Upsert
         * =========================
         */

        Optional<FantasyLeague> existingLeague =
                fantasyLeagueRepository
                        .findByPlatformAndExternalLeagueId(

                                FantasyPlatform.SLEEPER,

                                leagueId
                        );


        FantasyLeague league;


        if (
                existingLeague.isPresent()
        ) {

            league =
                    existingLeague.get();


            league.updateFromExternal(

                    leagueName,

                    season,

                    totalTeams,

                    status,

                    scoringFormat,

                    scoringSettingsJson
            );

        } else {

            league =
                    new FantasyLeague(

                            FantasyPlatform.SLEEPER,

                            leagueId,

                            leagueName,

                            season,

                            totalTeams,

                            status,

                            scoringFormat,

                            scoringSettingsJson
                    );
        }


        league =
                fantasyLeagueRepository
                        .save(
                                league
                        );


        /*
         * =========================
         * User Map
         * =========================
         */

        Map<String, String> displayNameByUserId =
                new HashMap<>();


        Map<String, String> teamNameByUserId =
                new HashMap<>();


        for (
                int i = 0;
                i < usersJson.size();
                i++
        ) {

            JsonNode user =
                    usersJson.get(i);


            String userId =
                    getText(
                            user,
                            "user_id"
                    );


            if (
                    userId == null
            ) {

                continue;
            }


            String displayName =
                    getText(
                            user,
                            "display_name"
                    );


            if (
                    displayName == null
            ) {

                displayName =
                        getText(
                                user,
                                "username"
                        );
            }


            displayNameByUserId.put(

                    userId,

                    displayName
            );


            JsonNode metadata =
                    user.get(
                            "metadata"
                    );


            String teamName =
                    getText(
                            metadata,
                            "team_name"
                    );


            if (
                    teamName != null
            ) {

                teamNameByUserId.put(

                        userId,

                        teamName
                );
            }
        }


        /*
         * =========================
         * Sleeper Player Map
         * =========================
         */

        List<PlayerExternalId> sleeperMappings =
                playerExternalIdRepository
                        .findByProvider(

                                PlayerProvider.SLEEPER
                        );


        Map<String, Player> playerBySleeperId =
                new HashMap<>();


        for (
                PlayerExternalId mapping
                :
                sleeperMappings
        ) {

            playerBySleeperId.put(

                    mapping.getExternalId(),

                    mapping.getPlayer()
            );
        }


        int teamsCreated =
                0;

        int teamsUpdated =
                0;

        int rosterPlayersSaved =
                0;

        int skippedUnmappedPlayers =
                0;


        Long myFantasyTeamId =
                null;


        /*
         * =========================
         * Rosters
         * =========================
         */

        for (
                int i = 0;
                i < rostersJson.size();
                i++
        ) {

            JsonNode roster =
                    rostersJson.get(i);


            String rosterId =
                    getText(
                            roster,
                            "roster_id"
                    );


            if (
                    rosterId == null
            ) {

                continue;
            }


            String ownerId =
                    getText(
                            roster,
                            "owner_id"
                    );


            String ownerDisplayName =
                    ownerId == null
                            ? null
                            : displayNameByUserId
                            .get(
                                    ownerId
                            );


            String teamName =
                    ownerId == null
                            ? null
                            : teamNameByUserId
                            .get(
                                    ownerId
                            );


            if (
                    teamName == null
                            ||
                            teamName.isBlank()
            ) {

                teamName =
                        ownerDisplayName;
            }


            if (
                    teamName == null
                            ||
                            teamName.isBlank()
            ) {

                teamName =
                        "Roster "
                                + rosterId;
            }


            Optional<FantasyTeam> existingTeam =
                    fantasyTeamRepository
                            .findByFantasyLeague_IdAndExternalTeamId(

                                    league.getId(),

                                    rosterId
                            );


            FantasyTeam fantasyTeam;


            if (
                    existingTeam.isPresent()
            ) {

                fantasyTeam =
                        existingTeam.get();


                fantasyTeam.updateFromExternal(

                        ownerId,

                        ownerDisplayName,

                        teamName
                );


                teamsUpdated++;

            } else {

                fantasyTeam =
                        new FantasyTeam(

                                league,

                                rosterId,

                                ownerId,

                                ownerDisplayName,

                                teamName
                        );


                teamsCreated++;
            }


            fantasyTeam =
                    fantasyTeamRepository
                            .save(
                                    fantasyTeam
                            );


            /*
             * 이 roster가 현재 사용자의
             * 실제 Team인지 확인합니다.
             */
            if (
                    currentUserId != null

                            &&

                            (
                                    currentUserId.equals(
                                            ownerId
                                    )

                                            ||

                                            arrayContains(

                                                    roster.get(
                                                            "co_owners"
                                                    ),

                                                    currentUserId
                                            )
                            )
            ) {

                myFantasyTeamId =
                        fantasyTeam.getId();
            }


            /*
             * 기존 로스터 초기화
             */
            fantasyRosterPlayerRepository
                    .deleteByFantasyTeam_Id(

                            fantasyTeam.getId()
                    );


            fantasyRosterPlayerRepository
                    .flush();


            List<String> starters =
                    readStringList(

                            roster,

                            "starters"
                    );


            Set<String> reserve =
                    readStringSet(

                            roster,

                            "reserve"
                    );


            Set<String> players =
                    readStringSet(

                            roster,

                            "players"
                    );


            /*
             * Starter ID → 실제 Lineup Slot
             */
            Map<String, String> starterSlotByPlayer =
                    new HashMap<>();


            Map<String, Integer> starterSequenceByPlayer =
                    new HashMap<>();


            for (
                    int starterIndex = 0;
                    starterIndex < starters.size();
                    starterIndex++
            ) {

                String sleeperPlayerId =
                        starters.get(
                                starterIndex
                        );


                String slot;


                if (
                        starterIndex
                                < starterSlots.size()
                ) {

                    slot =
                            normalizeLineupSlot(

                                    starterSlots.get(
                                            starterIndex
                                    )
                            );

                } else {

                    slot =
                            null;
                }


                starterSlotByPlayer.put(

                        sleeperPlayerId,

                        slot
                );


                starterSequenceByPlayer.put(

                        sleeperPlayerId,

                        starterIndex
                );
            }


            List<FantasyRosterPlayer> rosterPlayers =
                    new ArrayList<>();


            for (
                    String sleeperPlayerId
                    :
                    players
            ) {

                Player player =
                        playerBySleeperId
                                .get(
                                        sleeperPlayerId
                                );


                if (
                        player == null
                ) {

                    skippedUnmappedPlayers++;

                    continue;
                }


                RosterPlayerStatus rosterStatus;

                String lineupSlot;

                int lineupOrder;


                /*
                 * =========================
                 * RESERVE
                 * =========================
                 */
                if (
                        reserve.contains(
                                sleeperPlayerId
                        )
                ) {

                    rosterStatus =
                            RosterPlayerStatus.RESERVE;

                    lineupSlot =
                            "RESERVE";

                    lineupOrder =
                            20000
                                    +
                                    positionRank(
                                            player.getPosition()
                                    );


                    /*
                     * =========================
                     * STARTER
                     * =========================
                     */
                } else if (
                        starterSlotByPlayer
                                .containsKey(
                                        sleeperPlayerId
                                )
                ) {

                    rosterStatus =
                            RosterPlayerStatus.STARTER;


                    lineupSlot =
                            starterSlotByPlayer
                                    .get(
                                            sleeperPlayerId
                                    );


                    /*
                     * Sleeper slot을 얻지 못했다면
                     * 실제 선수 Position 사용
                     */
                    if (
                            lineupSlot == null
                                    ||
                                    lineupSlot.isBlank()
                    ) {

                        lineupSlot =
                                player.getPosition();
                    }


                    int sequence =
                            starterSequenceByPlayer
                                    .getOrDefault(

                                            sleeperPlayerId,

                                            0
                                    );


                    lineupOrder =

                            slotRank(
                                    lineupSlot
                            )
                                    * 100

                                    +

                                    sequence;


                    /*
                     * =========================
                     * BENCH
                     * =========================
                     */
                } else {

                    rosterStatus =
                            RosterPlayerStatus.BENCH;

                    lineupSlot =
                            "BENCH";


                    /*
                     * Bench에서는 FLEX라는
                     * 실제 자리가 존재하지 않으므로
                     * 선수의 원래 Position 순서로 정렬합니다.
                     */
                    lineupOrder =

                            10000

                                    +

                                    positionRank(
                                            player.getPosition()
                                    );
                }


                rosterPlayers.add(

                        new FantasyRosterPlayer(

                                fantasyTeam,

                                player,

                                rosterStatus,

                                lineupSlot,

                                lineupOrder
                        )
                );
            }


            fantasyRosterPlayerRepository
                    .saveAll(
                            rosterPlayers
                    );


            rosterPlayersSaved +=
                    rosterPlayers.size();
        }


        return new SleeperLeagueSyncResult(

                league.getId(),

                myFantasyTeamId,

                leagueId,

                leagueName,

                season,

                scoringFormat,

                rostersJson.size(),

                teamsCreated,

                teamsUpdated,

                rosterPlayersSaved,

                skippedUnmappedPlayers
        );
    }


    /**
     * Sleeper Username →
     * 고정 User ID
     */
    private String getSleeperUserId(
            String username
    ) {

        if (
                username == null
                        ||
                        username.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Sleeper username is required"
            );
        }


        JsonNode user =
                getJson(

                        SLEEPER_API
                                + "/user/"
                                + encodePath(
                                username.trim()
                        )
                );


        String userId =
                getText(
                        user,
                        "user_id"
                );


        if (
                userId == null
        ) {

            throw new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Sleeper user not found"
            );
        }


        return userId;
    }


    /**
     * League의 roster_positions 중
     * 실제 Starter 슬롯만 가져옵니다.
     *
     * BN / IR / TAXI 등은 제외합니다.
     */
    private List<String> readStarterSlots(
            JsonNode leagueJson
    ) {

        JsonNode positions =
                leagueJson.get(
                        "roster_positions"
                );


        List<String> result =
                new ArrayList<>();


        if (
                positions == null
                        ||
                        !positions.isArray()
        ) {

            return result;
        }


        for (
                int i = 0;
                i < positions.size();
                i++
        ) {

            String position =
                    positions
                            .get(i)
                            .asString();


            if (
                    position == null
                            ||
                            position.isBlank()
            ) {

                continue;
            }


            String normalized =
                    position.toUpperCase();


            if (
                    normalized.equals("BN")
                            ||
                            normalized.equals("BENCH")
                            ||
                            normalized.equals("IR")
                            ||
                            normalized.equals("TAXI")
            ) {

                continue;
            }


            result.add(
                    normalized
            );
        }


        return result;
    }


    /**
     * Sleeper의 다양한 FLEX 이름을
     * 화면용 기본 FLEX로 통일합니다.
     */
    private String normalizeLineupSlot(
            String slot
    ) {

        if (
                slot == null
        ) {

            return null;
        }


        String normalized =
                slot.toUpperCase();


        if (
                normalized.contains(
                        "FLEX"
                )
        ) {

            return "FLEX";
        }


        if (
                normalized.equals("DST")
        ) {

            return "DEF";
        }


        return normalized;
    }


    /**
     * Starter 정렬:
     *
     * QB
     * RB
     * WR
     * TE
     * FLEX
     * K
     * DEF
     */
    private int slotRank(
            String slot
    ) {

        if (
                slot == null
        ) {

            return 99;
        }


        return switch (
                normalizeLineupSlot(
                        slot
                )
                ) {

            case "QB" ->
                    1;

            case "RB" ->
                    2;

            case "WR" ->
                    3;

            case "TE" ->
                    4;

            case "FLEX" ->
                    5;

            case "K" ->
                    6;

            case "DEF" ->
                    7;

            default ->
                    99;
        };
    }


    /**
     * Bench / Reserve 정렬용
     */
    private int positionRank(
            String position
    ) {

        if (
                position == null
        ) {

            return 99;
        }


        return switch (
                position.toUpperCase()
                ) {

            case "QB" ->
                    100;

            case "RB" ->
                    200;

            case "WR" ->
                    300;

            case "TE" ->
                    400;

            case "K" ->
                    600;

            case "DEF", "DST" ->
                    700;

            default ->
                    990;
        };
    }


    private boolean arrayContains(

            JsonNode array,

            String value
    ) {

        if (
                array == null
                        ||
                        !array.isArray()
                        ||
                        value == null
        ) {

            return false;
        }


        for (
                int i = 0;
                i < array.size();
                i++
        ) {

            if (
                    value.equals(
                            array.get(i)
                                    .asString()
                    )
            ) {

                return true;
            }
        }


        return false;
    }


    private List<String> readStringList(

            JsonNode node,

            String fieldName
    ) {

        List<String> result =
                new ArrayList<>();


        if (
                node == null
        ) {

            return result;
        }


        JsonNode array =
                node.get(
                        fieldName
                );


        if (
                array == null
                        ||
                        !array.isArray()
        ) {

            return result;
        }


        for (
                int i = 0;
                i < array.size();
                i++
        ) {

            String value =
                    array.get(i)
                            .asString();


            if (
                    value != null
                            &&
                            !value.isBlank()
            ) {

                result.add(
                        value.trim()
                );
            }
        }


        return result;
    }


    private Set<String> readStringSet(

            JsonNode node,

            String fieldName
    ) {

        return new LinkedHashSet<>(

                readStringList(
                        node,
                        fieldName
                )
        );
    }


    private ScoringFormat detectScoringFormat(
            double receptionPoints
    ) {

        if (
                Math.abs(
                        receptionPoints - 1.0
                )
                        < 0.001
        ) {

            return ScoringFormat.PPR;
        }


        if (
                Math.abs(
                        receptionPoints - 0.5
                )
                        < 0.001
        ) {

            return ScoringFormat.HALF_PPR;
        }


        if (
                Math.abs(
                        receptionPoints
                )
                        < 0.001
        ) {

            return ScoringFormat.STANDARD;
        }


        if (
                receptionPoints >= 0.75
        ) {

            return ScoringFormat.PPR;
        }


        if (
                receptionPoints >= 0.25
        ) {

            return ScoringFormat.HALF_PPR;
        }


        return ScoringFormat.STANDARD;
    }


    private String getText(

            JsonNode node,

            String fieldName
    ) {

        if (
                node == null
                        ||
                        node.isNull()
        ) {

            return null;
        }


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


    private int getInt(

            JsonNode node,

            String fieldName,

            int defaultValue
    ) {

        String value =
                getText(
                        node,
                        fieldName
                );


        if (
                value == null
        ) {

            return defaultValue;
        }


        try {

            return Integer.parseInt(
                    value
            );

        } catch (
                NumberFormatException e
        ) {

            return defaultValue;
        }
    }


    private double getDouble(

            JsonNode node,

            String fieldName,

            double defaultValue
    ) {

        String value =
                getText(
                        node,
                        fieldName
                );


        if (
                value == null
        ) {

            return defaultValue;
        }


        try {

            return Double.parseDouble(
                    value
            );

        } catch (
                NumberFormatException e
        ) {

            return defaultValue;
        }
    }


    private String toJson(
            JsonNode node
    ) {

        if (
                node == null
                        ||
                        node.isNull()
        ) {

            return "{}";
        }


        try {

            return jsonMapper
                    .writeValueAsString(
                            node
                    );

        } catch (
                Exception e
        ) {

            return "{}";
        }
    }


    private String encodePath(
            String value
    ) {

        return URLEncoder
                .encode(

                        value,

                        StandardCharsets.UTF_8
                )

                .replace(
                        "+",
                        "%20"
                );
    }


    private JsonNode getJson(
            String url
    ) {

        try {

            HttpRequest request =
                    HttpRequest
                            .newBuilder()

                            .uri(
                                    URI.create(
                                            url
                                    )
                            )

                            .timeout(
                                    Duration.ofSeconds(30)
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
                    response.statusCode()
                            == 404
            ) {

                throw new ResponseStatusException(

                        HttpStatus.NOT_FOUND,

                        "Sleeper resource not found"
                );
            }


            if (
                    response.statusCode() < 200
                            ||
                            response.statusCode() >= 300
            ) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_GATEWAY,

                        "Sleeper API error: "
                                + response.statusCode()
                );
            }


            return jsonMapper
                    .readTree(
                            response.body()
                    );


        } catch (
                ResponseStatusException e
        ) {

            throw e;


        } catch (
                InterruptedException e
        ) {

            Thread.currentThread()
                    .interrupt();


            throw new ResponseStatusException(

                    HttpStatus.BAD_GATEWAY,

                    "Sleeper API request interrupted"
            );


        } catch (
                Exception e
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_GATEWAY,

                    "Unable to communicate with Sleeper API"
            );
        }
    }
}