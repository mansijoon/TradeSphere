package com.marketforge.trading.controller;

import com.marketforge.trading.service.PortfolioService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{accountId}/{instrumentId}")
    public ResponseEntity<PortfolioService.PortfolioResult> getPortfolio(
            @PathVariable UUID accountId,
            @PathVariable UUID instrumentId,
            @RequestParam @NotNull @Positive BigDecimal currentMarketPrice
    ) {
        return ResponseEntity.ok(
                portfolioService.calculate(
                        accountId,
                        instrumentId,
                        currentMarketPrice
                )
        );
    }
}
