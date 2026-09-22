package com.taegun.fantasy.controller;

import org.springframework.web.bind.annotation.*;

/**
 * 서버 작동 확인용 API입니다.
 */
@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {

        return "Fantasy Draft 서버가 정상적으로 실행 중입니다!";
    }
}