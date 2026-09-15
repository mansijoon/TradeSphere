package com.marketforge.trading;

import com.marketforge.trading.domain.Order;
import com.marketforge.trading.domain.OrderSide;
import com.marketforge.trading.domain.OrderStatus;
import com.marketforge.trading.domain.OrderType;
import com.marketforge.trading.domain.TimeInForce;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderFillTest {

    private Order order() {
        return new Order(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "FILL-TEST-001",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("100.00"),
                new BigDecimal("10.00")
        );
    }

    @Test
    void partialFillUpdatesQuantitiesAndStatus() {
        Order order = order();

        order.applyFill(new BigDecimal("4.00"));

        assertEquals(new BigDecimal("4.00"), order.getFilledQuantity());
        assertEquals(new BigDecimal("6.00"), order.getRemainingQuantity());
        assertEquals(OrderStatus.PARTIALLY_FILLED, order.getStatus());
    }

    @Test
    void fullFillMarksOrderFilled() {
        Order order = order();

        order.applyFill(new BigDecimal("10.00"));

        assertEquals(new BigDecimal("10.00"), order.getFilledQuantity());
        assertEquals(0, order.getRemainingQuantity().compareTo(BigDecimal.ZERO));
        assertEquals(OrderStatus.FILLED, order.getStatus());
    }

    @Test
    void overfillIsRejected() {
        Order order = order();

        assertThrows(
                IllegalArgumentException.class,
                () -> order.applyFill(new BigDecimal("10.01"))
        );
    }
}
