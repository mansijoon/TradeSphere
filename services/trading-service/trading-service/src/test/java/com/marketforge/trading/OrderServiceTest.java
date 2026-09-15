package com.marketforge.trading;

import com.marketforge.trading.domain.*;
import com.marketforge.trading.repository.OrderRepository;
import com.marketforge.trading.repository.TradingAccountRepository;
import com.marketforge.trading.messaging.OrderEventProducer;
import com.marketforge.trading.service.OrderService;
import com.marketforge.trading.service.OrderValidationService;
import com.marketforge.trading.client.RiskClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Test
    void createReturnsExistingOrderForDuplicateClientOrderId() {
        OrderRepository repository = mock(OrderRepository.class);
        TradingAccountRepository accountRepository = mock(TradingAccountRepository.class);

        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.randomUUID();

        Order existing = new Order(
                accountId,
                instrumentId,
                "CLIENT-001",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("100"),
                new BigDecimal("10")
        );

        when(repository.findByAccountIdAndClientOrderId(
                accountId, "CLIENT-001"
        )).thenReturn(Optional.of(existing));

        OrderValidationService validationService =
                new OrderValidationService();

        RiskClient riskClient = mock(RiskClient.class);

        com.marketforge.trading.messaging.OrderEventProducer eventProducer = mock(com.marketforge.trading.messaging.OrderEventProducer.class);

        OrderService service =
                new OrderService(repository, validationService, riskClient, accountRepository, eventProducer);

        assertSame(existing, service.create(existing));
        verify(repository, never()).save(any());
        verify(eventProducer, never()).publish(any());
    }
}
