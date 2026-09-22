package com.taegun.fantasy.service;

import com.taegun.fantasy.league.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;


/**
 * DB에 저장된 Fantasy League를
 * Frontend용 형태로 조회합니다.
 */
@Service
public class FantasyLeagueQueryService {

    private final FantasyLeagueRepository
            fantasyLeagueRepository;

    private final FantasyTeamRepository
            fantasyTeamRepository;

    private final FantasyRosterPlayerRepository
            fantasyRosterPlayerRepository;

    private int statusRank(
            FantasyRosterPlayer rosterPlayer
    ) {

        return switch (
                rosterPlayer.getStatus()
                ) {

            case STARTER ->
                    0;

            case BENCH ->
                    1;

            case RESERVE ->
                    2;
        };
    }


    public FantasyLeagueQueryService(

            FantasyLeagueRepository fantasyLeagueRepository,

            FantasyTeamRepository fantasyTeamRepository,

            FantasyRosterPlayerRepository
                    fantasyRosterPlayerRepository
    ) {

        this.fantasyLeagueRepository =
                fantasyLeagueRepository;

        this.fantasyTeamRepository =
                fantasyTeamRepository;

        this.fantasyRosterPlayerRepository =
                fantasyRosterPlayerRepository;
    }


    /**
     * League + Team + Roster 전체 조회
     */
    @Transactional(readOnly = true)
    public FantasyLeagueDetailResponse
    getLeagueDetail(
            Long fantasyLeagueId
    ) {

        FantasyLeague league =
                fantasyLeagueRepository
                        .findById(
                                fantasyLeagueId
                        )
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Fantasy League not found"
                                )

                        );


        List<FantasyTeam> teams =
                fantasyTeamRepository
                        .findByFantasyLeague_IdOrderByIdAsc(

                                fantasyLeagueId
                        );


        List<FantasyTeamResponse>
                teamResponses =
                new ArrayList<>();


        for (
                FantasyTeam team
                :
                teams
        ) {

            List<FantasyRosterPlayer>
                    roster =
                    fantasyRosterPlayerRepository
                            .findByFantasyTeam_IdOrderByIdAsc(

                                    team.getId()
                            );
            /*
             * 화면 표시 순서:
             *
             * STARTER
             *    QB
             *    RB
             *    WR
             *    TE
             *    FLEX
             *    K
             *    DEF
             *
             * BENCH
             *    QB
             *    RB
             *    WR
             *    TE
             *    K
             *    DEF
             *
             * RESERVE
             */
            roster.sort(

                    java.util.Comparator

                            .comparingInt(
                                    this::statusRank
                            )

                            .thenComparingInt(
                                    rosterPlayer -> {

                                        Integer order =
                                                rosterPlayer
                                                        .getLineupOrder();


                                        return order == null
                                                ? Integer.MAX_VALUE
                                                : order;
                                    }
                            )

                            .thenComparing(
                                    rosterPlayer ->
                                            rosterPlayer
                                                    .getPlayer()
                                                    .getName()
                            )
            );


            List<FantasyRosterPlayerResponse>
                    rosterResponses =
                    roster.stream()

                            .map(
                                    FantasyRosterPlayerResponse::from
                            )

                            .toList();


            teamResponses.add(

                    new FantasyTeamResponse(

                            team.getId(),

                            team.getExternalTeamId(),

                            team.getOwnerExternalUserId(),

                            team.getOwnerDisplayName(),

                            team.getTeamName(),

                            rosterResponses
                    )
            );
        }


        return new FantasyLeagueDetailResponse(

                league.getId(),

                league.getPlatform(),

                league.getExternalLeagueId(),

                league.getName(),

                league.getSeason(),

                league.getTotalTeams(),

                league.getStatus(),

                league.getScoringFormat(),

                teamResponses
        );
    }
}