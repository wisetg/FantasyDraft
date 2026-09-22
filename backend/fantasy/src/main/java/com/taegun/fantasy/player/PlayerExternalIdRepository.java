package com.taegun.fantasy.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * player_external_ids 테이블에 접근합니다.
 */
public interface PlayerExternalIdRepository
        extends JpaRepository<PlayerExternalId, Long> {


    /**
     * 특정 선수의 모든 External ID
     */
    List<PlayerExternalId>
    findByPlayer_IdOrderByProviderAsc(
            Long playerId
    );


    /**
     * 특정 선수의 특정 Provider ID
     */
    Optional<PlayerExternalId>
    findByPlayer_IdAndProvider(

            Long playerId,

            PlayerProvider provider
    );


    /**
     * Provider + External ID로
     * 연결 정보를 찾습니다.
     */
    Optional<PlayerExternalId>
    findByProviderAndExternalId(

            PlayerProvider provider,

            String externalId
    );


    /**
     * 특정 Provider의 모든 연결을 가져옵니다.
     *
     * 이번 GSIS 동기화에서
     * Sleeper 연결을 한꺼번에 가져올 때 사용합니다.
     */
    List<PlayerExternalId>
    findByProvider(
            PlayerProvider provider
    );
}