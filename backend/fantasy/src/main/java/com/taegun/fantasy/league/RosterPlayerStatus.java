package com.taegun.fantasy.league;

/**
 * Fantasy Team에서
 * 선수의 로스터 상태입니다.
 */
public enum RosterPlayerStatus {

    /**
     * 선발
     */
    STARTER,


    /**
     * 일반 벤치
     */
    BENCH,


    /**
     * IR / Reserve
     */
    RESERVE
}