package com.taegun.fantasy.player;

import jakarta.persistence.*;

/**
 * 우리 서비스의 Player와
 * 외부 서비스의 Player ID를 연결합니다.
 *
 * 예:
 *
 * Player
 * id = 1
 * name = Justin Jefferson
 *
 * PlayerExternalId
 * provider = SLEEPER
 * externalId = "외부 선수 ID"
 */
@Entity
@Table(
        name = "player_external_ids",

        uniqueConstraints = {

                /*
                 * 같은 Provider의 같은 External ID가
                 * 두 선수에게 연결되는 것을 방지합니다.
                 *
                 * 예:
                 *
                 * SLEEPER / 12345
                 *
                 * 는 DB에 하나만 존재해야 합니다.
                 */
                @UniqueConstraint(
                        name = "uk_external_provider_id",
                        columnNames = {
                                "provider",
                                "external_id"
                        }
                ),


                /*
                 * 한 선수가 같은 Provider ID를
                 * 여러 개 갖는 것을 방지합니다.
                 *
                 * Player 1은
                 *
                 * SLEEPER ID 하나
                 * ESPN ID 하나
                 *
                 * 를 가질 수 있습니다.
                 */
                @UniqueConstraint(
                        name = "uk_player_provider",
                        columnNames = {
                                "player_id",
                                "provider"
                        }
                )
        }
)
public class PlayerExternalId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * 우리 DB의 Player와 연결합니다.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "player_id",
            nullable = false
    )
    private Player player;


    /**
     * 외부 서비스 종류
     *
     * DB에는
     *
     * SLEEPER
     * ESPN
     *
     * 문자열로 저장합니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private PlayerProvider provider;


    /**
     * 외부 서비스에서 사용하는
     * 선수 ID입니다.
     */
    @Column(
            name = "external_id",
            nullable = false,
            length = 100
    )
    private String externalId;


    /**
     * JPA 기본 생성자
     */
    public PlayerExternalId() {
    }


    public PlayerExternalId(

            Player player,

            PlayerProvider provider,

            String externalId
    ) {

        this.player =
                player;

        this.provider =
                provider;

        this.externalId =
                externalId;
    }


    public Long getId() {

        return id;
    }


    public Player getPlayer() {

        return player;
    }


    public PlayerProvider getProvider() {

        return provider;
    }


    public String getExternalId() {

        return externalId;
    }


    /**
     * 외부 ID가 변경되었을 때
     * 갱신하기 위해 사용합니다.
     */
    public void setExternalId(
            String externalId
    ) {

        this.externalId =
                externalId;
    }
}