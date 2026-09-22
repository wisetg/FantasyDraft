package com.taegun.fantasy.league;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface FantasyLeagueRepository
        extends JpaRepository<FantasyLeague, Long> {


    /**
     * 외부 League ID로
     * 우리 League를 찾습니다.
     */
    Optional<FantasyLeague>
    findByPlatformAndExternalLeagueId(

            FantasyPlatform platform,

            String externalLeagueId
    );


    /**
     * 특정 플랫폼 / 시즌의
     * League 목록
     */
    List<FantasyLeague>
    findByPlatformAndSeasonOrderByNameAsc(

            FantasyPlatform platform,

            int season
    );
}