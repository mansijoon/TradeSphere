package com.marketforge.market.service;

import com.marketforge.market.domain.MarketTick;
import com.marketforge.market.domain.MarketTickEntity;
import com.marketforge.market.repository.MarketTickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketDataServiceTest {

    private MarketTickRepository repository;
    private MarketDataService service;

    private UUID instrumentId;

    @BeforeEach
    void setUp() {
        repository = mock(MarketTickRepository.class);
        service = new MarketDataService(repository);
        instrumentId = UUID.randomUUID();
    }

    @Test
    void recordsTick() {
        MarketTick tick = new MarketTick(
                instrumentId,
                new BigDecimal("101.25"),
                new BigDecimal("10"),
                OffsetDateTime.now()
        );

        MarketTickEntity saved = new MarketTickEntity(
                instrumentId,
                tick.price(),
                tick.quantity(),
                null,
                null,
                tick.timestamp()
        );

        when(repository.save(any(MarketTickEntity.class)))
                .thenReturn(saved);

        MarketTickEntity result = service.record(tick);

        assertSame(saved, result);
        assertEquals(instrumentId, result.getInstrumentId());
        assertEquals(new BigDecimal("101.25"), result.getPrice());
        assertEquals(new BigDecimal("10"), result.getQuantity());

        verify(repository).save(any(MarketTickEntity.class));
    }

    @Test
    void returnsLatestTick() {
        MarketTickEntity latest = new MarketTickEntity(
                instrumentId,
                new BigDecimal("105"),
                new BigDecimal("5"),
                null,
                null,
                OffsetDateTime.now()
        );

        when(repository
                .findTop100ByInstrumentIdOrderByTimestampDesc(instrumentId))
                .thenReturn(List.of(latest));

        assertSame(latest, service.latest(instrumentId));
    }

    @Test
    void returnsNullWhenNoTicksExist() {
        when(repository
                .findTop100ByInstrumentIdOrderByTimestampDesc(instrumentId))
                .thenReturn(List.of());

        assertNull(service.latest(instrumentId));
    }

    @Test
    void returnsRecentTicks() {
        MarketTickEntity tick = new MarketTickEntity(
                instrumentId,
                new BigDecimal("105"),
                new BigDecimal("5"),
                null,
                null,
                OffsetDateTime.now()
        );

        when(repository
                .findTop100ByInstrumentIdOrderByTimestampDesc(instrumentId))
                .thenReturn(List.of(tick));

        List<MarketTickEntity> result = service.recent(instrumentId);

        assertEquals(1, result.size());
        assertSame(tick, result.getFirst());
    }
}
