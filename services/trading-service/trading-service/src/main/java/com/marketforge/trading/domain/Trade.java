package com.marketforge.trading.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_trade_id", nullable = false, length = 64)
    private String externalTradeId;

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Column(name = "buy_order_id", nullable = false)
    private UUID buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private UUID sellOrderId;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "executed_at", nullable = false)
    private OffsetDateTime executedAt;

    protected Trade() {}

    public Trade(
            UUID instrumentId,
            UUID buyOrderId,
            UUID sellOrderId,
            BigDecimal price,
            BigDecimal quantity,
            OffsetDateTime executedAt,
            String externalTradeId
    ) {
        this.externalTradeId = externalTradeId;
        this.instrumentId = instrumentId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.executedAt = executedAt;
    }

    public UUID getId() { return id; }
    public String getExternalTradeId() { return externalTradeId; }
    public UUID getInstrumentId() { return instrumentId; }
    public UUID getBuyOrderId() { return buyOrderId; }
    public UUID getSellOrderId() { return sellOrderId; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getQuantity() { return quantity; }
    public OffsetDateTime getExecutedAt() { return executedAt; }
}
