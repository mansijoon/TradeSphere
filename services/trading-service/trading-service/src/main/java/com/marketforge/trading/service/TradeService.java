package com.marketforge.trading.service;

import com.marketforge.trading.domain.Order;
import com.marketforge.trading.domain.Trade;
import com.marketforge.trading.repository.OrderRepository;
import com.marketforge.trading.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final OrderRepository orderRepository;

    public TradeService(
            TradeRepository tradeRepository,
            OrderRepository orderRepository
    ) {
        this.tradeRepository = tradeRepository;
        this.orderRepository = orderRepository;
    }

    public List<Trade> getTradeHistory(UUID accountId) {
        List<UUID> orderIds = orderRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(Order::getId)
                .toList();

        if (orderIds.isEmpty()) {
            return List.of();
        }

        return tradeRepository
                .findByBuyOrderIdInOrSellOrderIdInOrderByExecutedAtDesc(
                        orderIds,
                        orderIds
                );
    }
}
