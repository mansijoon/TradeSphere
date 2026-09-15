package com.marketforge.trading.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "orders",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_client_order",
        columnNames = {"account_id", "client_order_id"}
    )
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Column(name = "client_order_id", nullable = false, length = 64)
    private String clientOrderId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "order_side")
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "order_type", nullable = false, columnDefinition = "order_type")
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "time_in_force", nullable = false, columnDefinition = "time_in_force")
    private TimeInForce timeInForce;

    @Column(precision = 20, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "filled_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal filledQuantity;

    @Column(name = "remaining_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal remainingQuantity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "order_status")
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Order() {}

    public Order(
            UUID accountId,
            UUID instrumentId,
            String clientOrderId,
            OrderSide side,
            OrderType orderType,
            TimeInForce timeInForce,
            BigDecimal price,
            BigDecimal quantity
    ) {
        this.accountId = accountId;
        this.instrumentId = instrumentId;
        this.clientOrderId = clientOrderId;
        this.side = side;
        this.orderType = orderType;
        this.timeInForce = timeInForce;
        this.price = price;
        this.quantity = quantity;
        this.filledQuantity = BigDecimal.ZERO;
        this.remainingQuantity = quantity;
        this.status = OrderStatus.CREATED;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public UUID getInstrumentId() { return instrumentId; }
    public String getClientOrderId() { return clientOrderId; }
    public OrderSide getSide() { return side; }
    public OrderType getOrderType() { return orderType; }
    public TimeInForce getTimeInForce() { return timeInForce; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getFilledQuantity() { return filledQuantity; }
    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public OrderStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void applyFill(BigDecimal fillQuantity) {
        if (fillQuantity == null || fillQuantity.signum() <= 0) {
            throw new IllegalArgumentException("Fill quantity must be positive");
        }

        if (fillQuantity.compareTo(remainingQuantity) > 0) {
            throw new IllegalArgumentException(
                    "Fill quantity exceeds remaining order quantity"
            );
        }

        filledQuantity = filledQuantity.add(fillQuantity);
        remainingQuantity = quantity.subtract(filledQuantity);
        status = remainingQuantity.signum() == 0
                ? OrderStatus.FILLED
                : OrderStatus.PARTIALLY_FILLED;
        updatedAt = OffsetDateTime.now();
    }
}
