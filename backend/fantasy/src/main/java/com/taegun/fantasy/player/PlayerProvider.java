package com.taegun.fantasy.player;

/**
 * 외부 Player ID 제공자를 구분합니다.
 */
public enum PlayerProvider {

    SLEEPER,

    ESPN,

    /**
     * NFL Game Statistics & Information System ID
     *
     * nflverse 경기 기록과
     * 우리 Player를 연결할 때 사용합니다.
     */
    GSIS
}