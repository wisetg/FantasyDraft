package com.taegun.fantasy.league;

/**
 * Fantasy League를 제공하는 외부 플랫폼입니다.
 *
 * PlayerProvider와 별개로 관리합니다.
 *
 * PlayerProvider:
 * 선수 ID가 어디에서 왔는지
 *
 * FantasyPlatform:
 * Fantasy League가 어디에서 왔는지
 */
public enum FantasyPlatform {

    SLEEPER,

    ESPN
}