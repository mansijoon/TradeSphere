package com.marketforge.trading.controller;

import com.marketforge.trading.domain.Trade;
import com.marketforge.trading.service.TradeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trades")
public class TradeController {

    private final TradeService service;

    public TradeController(TradeService service) {
        this.service = service;
    }

    @GetMapping("/history/{accountId}")
    public List<Trade> getTradeHistory(@PathVariable UUID accountId) {
        return service.getTradeHistory(accountId);
    }
}
