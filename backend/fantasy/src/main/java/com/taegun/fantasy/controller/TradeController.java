package com.taegun.fantasy.controller;

import com.taegun.fantasy.service.TradeSimulationService;
import com.taegun.fantasy.trade.TradeRequest;
import com.taegun.fantasy.trade.TradeResult;
import org.springframework.web.bind.annotation.*;

/**
 * 트레이드 시뮬레이션 API입니다.
 */
@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeSimulationService tradeSimulationService;


    public TradeController(
            TradeSimulationService tradeSimulationService
    ) {

        this.tradeSimulationService =
                tradeSimulationService;
    }


    /**
     * POST /api/trades/simulate
     */
    @PostMapping("/simulate")
    public TradeResult simulate(
            @RequestBody TradeRequest request
    ) {

        return tradeSimulationService.simulate(request);
    }
}