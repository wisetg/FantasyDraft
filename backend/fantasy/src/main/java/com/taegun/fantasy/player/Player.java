package com.taegun.fantasy.player;

import jakarta.persistence.*;

/**
 * 선수의 기본 정보를 저장하는 Entity입니다.
 *
 * 경기 기록은 PlayerStats에서 별도로 관리합니다.
 */
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // 선수 이름
    private String name;


    // 포지션
    private String position;


    // 실제 NFL 소속 팀
    private String team;


    /**
     * JPA 기본 생성자
     */
    public Player() {
    }


    public Player(
            String name,
            String position,
            String team
    ) {

        this.name = name;
        this.position = position;
        this.team = team;
    }


    public Long getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public String getPosition() {
        return position;
    }


    public String getTeam() {
        return team;
    }


    /**
     * 외부 데이터와 동기화할 때
     * 선수 기본 정보를 갱신합니다.
     *
     * 예:
     *
     * 팀 이동
     * 이름 변경
     * 포지션 변경
     */
    public void updateBasicInfo(
            String name,
            String position,
            String team
    ) {

        this.name = name;
        this.position = position;
        this.team = team;
    }
}