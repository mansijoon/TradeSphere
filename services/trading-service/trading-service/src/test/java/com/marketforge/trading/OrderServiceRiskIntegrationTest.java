package com.marketforge.trading;

import com.marketforge.trading.domain.*;
import com.marketforge.trading.exception.InvalidOrderException;
import com.marketforge.trading.messaging.OrderEventProducer;
import com.marketforge.trading.repository.OrderRepository;
import com.marketforge.trading.repository.TradingAccountRepository;
import com.marketforge.trading.service.OrderService;
import com.marketforge.trading.service.OrderValidationService;
import com.marketforge.trading.service.OrderCacheService;
import com.marketforge.trading.client.RiskClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderServiceRiskIntegrationTest {

    @Test
    void newOrderRunsRiskValidationBeforePersistence() {
        OrderRepository repository = mock(OrderRepository.class);
        TradingAccountRepository accountRepository = mock(TradingAccountRepository.class);
        RiskClient riskClient = mock(RiskClient.class);
        OrderEventProducer producer = mock(OrderEventProducer.class);
        OrderCacheService cacheService = mock(OrderCacheService.class);

        UUID accountId = UUID.randomUUID();

        Order order = new Order(
                accountId,
                UUID.randomUUID(),
                "RISK-INTEGRATION-001",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("100"),
                new BigDecimal("10")
        );

        when(repository.findByAccountIdAndClientOrderId(
                accountId, "RISK-INTEGRATION-001"
        )).thenReturn(Optional.empty());

        when(repository.save(order)).thenReturn(order);

        when(accountRepository.reserveBuyingPower(
                accountId,
                new BigDecimal("1000")
        )).thenReturn(1);

        OrderService service = new OrderService(
                repository,
                new OrderValidationService(),
                riskClient,
                accountRepository,
                producer,
                cacheService
        );

        service.create(order);

        verify(riskClient).validate(order);
        verify(repository).save(order);
        verify(producer).publish(order);
    }

    @Test
    void riskRejectionPreventsPersistenceAndEventPublishing() {
        OrderRepository repository = mock(OrderRepository.class);
        TradingAccountRepository accountRepository = mock(TradingAccountRepository.class);
        RiskClient riskClient = mock(RiskClient.class);
        OrderEventProducer producer = mock(OrderEventProducer.class);
        OrderCacheService cacheService = mock(OrderCacheService.class);

        UUID accountId = UUID.randomUUID();

        Order order = new Order(
                accountId,
                UUID.randomUUID(),
                "RISK-INTEGRATION-002",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("100"),
                new BigDecimal("10")
        );

        when(repository.findByAccountIdAndClientOrderId(
                accountId, "RISK-INTEGRATION-002"
        )).thenReturn(Optional.empty());

        doThrow(new InvalidOrderException("Insufficient buying power"))
                .when(riskClient).validate(order);

        OrderService service = new OrderService(
                repository,
                new OrderValidationService(),
                riskClient,
                accountRepository,
                producer,
                cacheService
        );

        assertThrows(InvalidOrderException.class, () -> service.create(order));

        verify(repository, never()).save(any());
        verify(producer, never()).publish(any());
    }

    @Test
    void duplicateOrderBypassesRiskValidation() {
        OrderRepository repository = mock(OrderRepository.class);
        TradingAccountRepository accountRepository = mock(TradingAccountRepository.class);
        RiskClient riskClient = mock(RiskClient.class);
        OrderEventProducer producer = mock(OrderEventProducer.class);
        OrderCacheService cacheService = mock(OrderCacheService.class);

        UUID accountId = UUID.randomUUID();

        Order existing = new Order(
                accountId,
                UUID.randomUUID(),
                "RISK-INTEGRATION-003",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("100"),
                new BigDecimal("10")
        );

        when(repository.findByAccountIdAndClientOrderId(
                accountId, "RISK-INTEGRATION-003"
        )).thenReturn(Optional.of(existing));

        OrderService service = new OrderService(
                repository,
                new OrderValidationService(),
                riskClient,
                accountRepository,
                producer,
                cacheService
        );

        service.create(existing);

        verify(riskClient, never()).validate(any());
        verify(repository, never()).save(any());
        verify(producer, never()).publish(any());
    }

    @Test
    void reservationFailurePreventsPersistenceAndEventPublishing() {
        OrderRepository repository = mock(OrderRepository.class);
        RiskClient riskClient = mock(RiskClient.class);
        TradingAccountRepository accountRepository =
                mock(TradingAccountRepository.class);
        OrderEventProducer producer = mock(OrderEventProducer.class);
        OrderCacheService cacheService = mock(OrderCacheService.class);

        UUID accountId = UUID.randomUUID();

        Order order = new Order(
                accountId,
                UUID.randomUUID(),
                "RESERVATION-FAIL-001",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("100"),
                new BigDecimal("10")
        );

        when(repository.findByAccountIdAndClientOrderId(
                accountId, "RESERVATION-FAIL-001"
        )).thenReturn(Optional.empty());

        when(accountRepository.reserveBuyingPower(
                accountId,
                new BigDecimal("1000")
        )).thenReturn(0);

        OrderService service = new OrderService(
                repository,
                new OrderValidationService(),
                riskClient,
                accountRepository,
                producer,
                cacheService
        );

        assertThrows(
                InvalidOrderException.class,
                () -> service.create(order)
        );

        verify(repository, never()).save(any());
        verify(producer, never()).publish(any());
    }
}

