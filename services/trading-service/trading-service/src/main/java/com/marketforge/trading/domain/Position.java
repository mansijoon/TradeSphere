package com.marketforge.trading.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "positions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_positions_account_instrument",
        columnNames = {"account_id", "instrument_id"}
    )
)
public class Position {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "average_entry_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal averageEntryPrice;

    @Column(name = "realized_pnl", nullable = false, precision = 20, scale = 8)
    private BigDecimal realizedPnl;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Position() {
    }

    public Position(
            UUID id,
            UUID accountId,
            UUID instrumentId,
            BigDecimal quantity,
            BigDecimal averageEntryPrice,
            BigDecimal realizedPnl,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.accountId = accountId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.averageEntryPrice = averageEntryPrice;
        this.realizedPnl = realizedPnl;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getAverageEntryPrice() {
        return averageEntryPrice;
    }

    public BigDecimal getRealizedPnl() {
        return realizedPnl;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
