package com.marketforge.trading;

import com.marketforge.trading.domain.*;
import com.marketforge.trading.exception.InvalidOrderException;
import com.marketforge.trading.service.OrderValidationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderValidationServiceTest {

    private final OrderValidationService service =
            new OrderValidationService();

    @Test
    void validLimitOrderPasses() {
        Order order = new Order(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CLIENT-001",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("100"),
                new BigDecimal("10")
        );

        assertDoesNotThrow(() -> service.validate(order));
    }

    @Test
    void limitOrderWithoutPriceFails() {
        Order order = new Order(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CLIENT-002",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                null,
                new BigDecimal("10")
        );

        assertThrows(
                InvalidOrderException.class,
                () -> service.validate(order)
        );
    }

    @Test
    void zeroQuantityFails() {
        Order order = new Order(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CLIENT-003",
                OrderSide.BUY,
                OrderType.MARKET,
                TimeInForce.DAY,
                null,
                BigDecimal.ZERO
        );

        assertThrows(
                InvalidOrderException.class,
                () -> service.validate(order)
        );
    }
}
