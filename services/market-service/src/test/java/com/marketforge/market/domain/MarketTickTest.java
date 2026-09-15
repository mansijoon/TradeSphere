package com.marketforge.market.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MarketTickTest {

    @Test
    void createsValidTick() {
        UUID instrumentId = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.now();

        MarketTick tick = new MarketTick(
                instrumentId,
                new BigDecimal("101.25"),
                new BigDecimal("10"),
                timestamp
        );

        assertEquals(instrumentId, tick.instrumentId());
        assertEquals(new BigDecimal("101.25"), tick.price());
        assertEquals(new BigDecimal("10"), tick.quantity());
        assertEquals(timestamp, tick.timestamp());
    }

    @Test
    void rejectsNonPositivePrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MarketTick(
                        UUID.randomUUID(),
                        BigDecimal.ZERO,
                        BigDecimal.ONE,
                        OffsetDateTime.now()
                )
        );
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MarketTick(
                        UUID.randomUUID(),
                        BigDecimal.ONE,
                        BigDecimal.ZERO,
                        OffsetDateTime.now()
                )
        );
    }
}
