package com.taegun.fantasy.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


/**
 * player_stats 테이블 접근 Repository입니다.
 */
public interface PlayerStatsRepository
        extends JpaRepository<PlayerStats, Long> {


    /**
     * 특정 선수의 전체 기록
     */
    List<PlayerStats>
    findByPlayerIdOrderBySeasonAscWeekAsc(
            Long playerId
    );


    /**
     * 특정 선수의 특정 시즌 기록
     */
    List<PlayerStats>
    findByPlayerIdAndSeasonOrderByWeekAsc(
            Long playerId,
            int season
    );


    /**
     * 특정 선수의 최신 기록
     */
    Optional<PlayerStats>
    findTopByPlayerIdOrderBySeasonDescWeekDesc(
            Long playerId
    );


    /**
     * 특정 선수 / 시즌 / Week의
     * 경기 기록 하나를 찾습니다.
     */
    Optional<PlayerStats>
    findByPlayer_IdAndSeasonAndWeek(

            Long playerId,

            int season,

            int week
    );


    /**
     * 특정 시즌의 모든 기록을 가져옵니다.
     *
     * nflverse 동기화 시
     * 이미 존재하는 기록인지 빠르게 확인합니다.
     */
    List<PlayerStats>
    findBySeason(
            int season
    );
}