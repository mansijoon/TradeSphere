package com.marketforge.trading;

import com.marketforge.trading.controller.OrderController;
import com.marketforge.trading.domain.*;
import com.marketforge.trading.dto.CreateOrderRequest;
import com.marketforge.trading.service.OrderService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Test
    void createBuildsOrderAndDelegatesToService() {
        OrderService service = mock(OrderService.class);

        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(
                accountId,
                instrumentId,
                "CLIENT-001",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("100"),
                new BigDecimal("10")
        );

        OrderController controller = new OrderController(service);

        when(service.create(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = controller.create(request);

        assertEquals(accountId, result.getAccountId());
        assertEquals(instrumentId, result.getInstrumentId());
        assertEquals("CLIENT-001", result.getClientOrderId());
        assertEquals(OrderSide.BUY, result.getSide());
        assertEquals(OrderType.LIMIT, result.getOrderType());

        verify(service).create(any(Order.class));
    }
}
