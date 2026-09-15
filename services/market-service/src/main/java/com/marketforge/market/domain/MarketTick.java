package com.marketforge.market.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MarketTick(
        UUID instrumentId,
        BigDecimal price,
        BigDecimal quantity,
        OffsetDateTime timestamp
) {
    public MarketTick {
        if (instrumentId == null) {
            throw new IllegalArgumentException("Instrument ID is required");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp is required");
        }
    }
}
