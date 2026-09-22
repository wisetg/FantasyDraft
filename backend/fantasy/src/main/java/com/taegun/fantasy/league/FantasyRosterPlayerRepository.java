package com.taegun.fantasy.league;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


/**
 * Fantasy Team의 실제 NFL Player 보유 정보를
 * 관리하는 Repository입니다.
 */
public interface FantasyRosterPlayerRepository
        extends JpaRepository<FantasyRosterPlayer, Long> {


    /**
     * 특정 Fantasy Team의 전체 로스터
     */
    List<FantasyRosterPlayer>
    findByFantasyTeam_IdOrderByIdAsc(
            Long fantasyTeamId
    );


    /**
     * 특정 Fantasy Team 안에서
     * 특정 Player를 찾습니다.
     */
    Optional<FantasyRosterPlayer>
    findByFantasyTeam_IdAndPlayer_Id(
            Long fantasyTeamId,
            Long playerId
    );


    /**
     * 특정 Fantasy Team의 로스터를
     * 모두 제거합니다.
     *
     * Sleeper 재동기화 과정에서 사용합니다.
     */
    void deleteByFantasyTeam_Id(
            Long fantasyTeamId
    );


    /**
     * 특정 League에 속한
     * 모든 Fantasy Team의 모든 Player를 가져옵니다.
     *
     * Trade Value V2에서
     *
     * "현재 이 League에서 이미 보유 중인 선수"
     *
     * 를 판별할 때 사용합니다.
     */
    List<FantasyRosterPlayer>
    findByFantasyTeam_FantasyLeague_Id(
            Long fantasyLeagueId
    );
}