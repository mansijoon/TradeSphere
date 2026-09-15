package com.marketforge.market.controller;

import com.marketforge.market.domain.MarketTickEntity;
import com.marketforge.market.service.MarketDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/market-data")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/{instrumentId}/latest")
    public ResponseEntity<MarketTickEntity> latest(
            @PathVariable UUID instrumentId
    ) {
        MarketTickEntity tick = marketDataService.latest(instrumentId);

        if (tick == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(tick);
    }

    @GetMapping("/{instrumentId}/history")
    public ResponseEntity<List<MarketTickEntity>> history(
            @PathVariable UUID instrumentId
    ) {
        return ResponseEntity.ok(
                marketDataService.recent(instrumentId)
        );
    }
}
