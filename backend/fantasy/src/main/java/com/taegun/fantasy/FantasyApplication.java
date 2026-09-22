package com.taegun.fantasy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Fantasy Draft 백엔드 서버의 시작점입니다.
 */
@SpringBootApplication
public class FantasyApplication {

    public static void main(String[] args) {

        // Spring Boot 서버 실행
        SpringApplication.run(FantasyApplication.class, args);
    }
}