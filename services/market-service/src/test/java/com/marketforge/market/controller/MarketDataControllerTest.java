package com.marketforge.market.controller;

import com.marketforge.market.domain.MarketTickEntity;
import com.marketforge.market.service.MarketDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketDataControllerTest {

    private MarketDataService service;
    private MarketDataController controller;

    private UUID instrumentId;

    @BeforeEach
    void setUp() {
        service = mock(MarketDataService.class);
        controller = new MarketDataController(service);
        instrumentId = UUID.randomUUID();
    }

    @Test
    void returnsLatestTick() {
        MarketTickEntity tick = new MarketTickEntity(
                instrumentId,
                new BigDecimal("101.25"),
                new BigDecimal("10"),
                new BigDecimal("101.20"),
                new BigDecimal("101.30"),
                OffsetDateTime.now()
        );

        when(service.latest(instrumentId)).thenReturn(tick);

        ResponseEntity<MarketTickEntity> response =
                controller.latest(instrumentId);

        assertEquals(200, response.getStatusCode().value());
        assertSame(tick, response.getBody());
        verify(service).latest(instrumentId);
    }

    @Test
    void returnsNotFoundWhenLatestTickDoesNotExist() {
        when(service.latest(instrumentId)).thenReturn(null);

        ResponseEntity<MarketTickEntity> response =
                controller.latest(instrumentId);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void returnsHistory() {
        MarketTickEntity tick = new MarketTickEntity(
                instrumentId,
                new BigDecimal("101.25"),
                new BigDecimal("10"),
                null,
                null,
                OffsetDateTime.now()
        );

        when(service.recent(instrumentId)).thenReturn(List.of(tick));

        ResponseEntity<List<MarketTickEntity>> response =
                controller.history(instrumentId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertSame(tick, response.getBody().getFirst());

        verify(service).recent(instrumentId);
    }
}
