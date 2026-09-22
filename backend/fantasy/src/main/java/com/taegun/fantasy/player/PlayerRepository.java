package com.taegun.fantasy.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


/**
 * players 테이블 Repository
 */
public interface PlayerRepository
        extends JpaRepository<Player, Long> {


    /**
     * 선수 이름 / 포지션 / 팀 검색
     */
    @Query("""
            SELECT p
            FROM Player p
            WHERE
                (
                    :search = ''
                    OR LOWER(p.name)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                )
            AND
                (
                    :position = ''
                    OR UPPER(p.position)
                    = UPPER(:position)
                )
            AND
                (
                    :team = ''
                    OR UPPER(p.team)
                    = UPPER(:team)
                )
            ORDER BY p.name ASC
            """)
    List<Player> searchPlayers(
            @Param("search") String search,
            @Param("position") String position,
            @Param("team") String team
    );


    /**
     * Position 목록
     */
    @Query("""
            SELECT DISTINCT p.position
            FROM Player p
            WHERE p.position IS NOT NULL
            AND p.position <> ''
            ORDER BY p.position ASC
            """)
    List<String> findDistinctPositions();


    /**
     * Team 목록
     */
    @Query("""
            SELECT DISTINCT p.team
            FROM Player p
            WHERE p.team IS NOT NULL
            AND p.team <> ''
            ORDER BY p.team ASC
            """)
    List<String> findDistinctTeams();


    /**
     * Sleeper 선수 동기화 시
     * 기존 Player를 찾습니다.
     */
    Optional<Player>
    findFirstByNameIgnoreCaseAndPositionIgnoreCaseAndTeamIgnoreCase(
            String name,
            String position,
            String team
    );


    /**
     * Team Defense 찾기
     *
     * 예:
     *
     * position = DEF
     * team     = MIN
     */
    Optional<Player>
    findFirstByPositionIgnoreCaseAndTeamIgnoreCase(
            String position,
            String team
    );
}