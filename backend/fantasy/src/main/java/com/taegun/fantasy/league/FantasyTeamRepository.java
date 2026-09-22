package com.taegun.fantasy.league;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface FantasyTeamRepository
        extends JpaRepository<FantasyTeam, Long> {


    /**
     * 특정 League의 모든 Fantasy Team
     */
    List<FantasyTeam>
    findByFantasyLeague_IdOrderByIdAsc(
            Long fantasyLeagueId
    );


    /**
     * League + 외부 Team ID
     *
     * Sleeper에서는:
     *
     * league_id + roster_id
     */
    Optional<FantasyTeam>
    findByFantasyLeague_IdAndExternalTeamId(

            Long fantasyLeagueId,

            String externalTeamId
    );
}