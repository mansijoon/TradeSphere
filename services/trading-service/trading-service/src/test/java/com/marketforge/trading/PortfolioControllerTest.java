package com.marketforge.trading;

import com.marketforge.trading.controller.PortfolioController;
import com.marketforge.trading.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortfolioControllerTest {

    private PortfolioService portfolioService;
    private PortfolioController controller;

    private UUID accountId;
    private UUID instrumentId;

    @BeforeEach
    void setUp() {
        portfolioService = mock(PortfolioService.class);
        controller = new PortfolioController(portfolioService);

        accountId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();
    }

    @Test
    void returnsCalculatedPortfolio() {
        var result = new PortfolioService.PortfolioResult(
                accountId,
                instrumentId,
                new BigDecimal("10"),
                new BigDecimal("100"),
                new BigDecimal("1000"),
                new BigDecimal("800"),
                new BigDecimal("1200"),
                new BigDecimal("200"),
                new BigDecimal("25"),
                new BigDecimal("2200")
        );

        when(portfolioService.calculate(
                accountId,
                instrumentId,
                new BigDecimal("120")
        )).thenReturn(result);

        var response = controller.getPortfolio(
                accountId,
                instrumentId,
                new BigDecimal("120")
        );

        assertEquals(200, response.getStatusCode().value());
        assertSame(result, response.getBody());

        verify(portfolioService).calculate(
                accountId,
                instrumentId,
                new BigDecimal("120")
        );
    }
}
