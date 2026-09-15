package com.marketforge.trading.service;

import com.marketforge.trading.domain.Order;
import com.marketforge.trading.client.RiskClient;
import com.marketforge.trading.messaging.OrderEventProducer;
import com.marketforge.trading.repository.OrderRepository;
import com.marketforge.trading.repository.TradingAccountRepository;
import com.marketforge.trading.domain.OrderSide;
import com.marketforge.trading.domain.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final OrderValidationService validationService;
    private final RiskClient riskClient;
    private final TradingAccountRepository accountRepository;
    private final OrderEventProducer orderEventProducer;
    private final OrderCacheService orderCacheService;

    public OrderService(
            OrderRepository repository,
            OrderValidationService validationService,
            RiskClient riskClient,
            TradingAccountRepository accountRepository,
            OrderEventProducer orderEventProducer,
            OrderCacheService orderCacheService
    ) {
        this.repository = repository;
        this.validationService = validationService;
        this.riskClient = riskClient;
        this.accountRepository = accountRepository;
        this.orderEventProducer = orderEventProducer;
        this.orderCacheService = orderCacheService;
    }

    @Transactional
    public Order create(Order order) {
        validationService.validate(order);

        var cachedOrderId = orderCacheService.getOrderId(
                order.getAccountId(),
                order.getClientOrderId()
        );

        if (cachedOrderId != null) {
            var cachedOrder = repository.findById(cachedOrderId);
            if (cachedOrder.isPresent()) {
                return cachedOrder.get();
            }

            orderCacheService.evict(
                    order.getAccountId(),
                    order.getClientOrderId()
            );
        }

        var existing = repository.findByAccountIdAndClientOrderId(
                order.getAccountId(),
                order.getClientOrderId()
        );

        if (existing.isPresent()) {
            orderCacheService.put(existing.get());
            return existing.get();
        }

        riskClient.validate(order);

        if (order.getSide() == OrderSide.BUY
                && order.getPrice() != null) {

            var requiredAmount =
                    order.getPrice().multiply(order.getQuantity());

            int reserved = accountRepository.reserveBuyingPower(
                    order.getAccountId(),
                    requiredAmount
            );

            if (reserved != 1) {
                throw new com.marketforge.trading.exception.InvalidOrderException(
                        "Unable to reserve buying power"
                );
            }
        }

        Order saved = repository.save(order);
        orderCacheService.put(saved);
        orderEventProducer.publish(saved);
        return saved;
    }

    public Order getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + id
                ));
    }

    public List<Order> getOpenOrders(UUID accountId) {
        return repository.findByAccountIdAndStatusInOrderByCreatedAtDesc(
                accountId,
                List.of(
                        OrderStatus.CREATED,
                        OrderStatus.ACCEPTED,
                        OrderStatus.PARTIALLY_FILLED
                )
        );
    }

    public List<Order> getOrderHistory(UUID accountId) {
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
