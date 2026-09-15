package com.marketforge.market.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "market_ticks",
    indexes = {
        @Index(
            name = "idx_market_ticks_instrument_time",
            columnList = "instrument_id,timestamp"
        )
    }
)
public class MarketTickEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "bid_price", precision = 20, scale = 8)
    private BigDecimal bidPrice;

    @Column(name = "ask_price", precision = 20, scale = 8)
    private BigDecimal askPrice;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    protected MarketTickEntity() {
    }

    public MarketTickEntity(
            UUID instrumentId,
            BigDecimal price,
            BigDecimal quantity,
            BigDecimal bidPrice,
            BigDecimal askPrice,
            OffsetDateTime timestamp
    ) {
        this.instrumentId = instrumentId;
        this.price = price;
        this.quantity = quantity;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public BigDecimal getAskPrice() {
        return askPrice;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
