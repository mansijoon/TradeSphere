package com.marketforge.trading.service;

import com.marketforge.trading.domain.Order;
import com.marketforge.trading.domain.OrderType;
import com.marketforge.trading.exception.InvalidOrderException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderValidationService {

    public void validate(Order order) {
        if (order.getQuantity() == null ||
                order.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException("Order quantity must be positive");
        }

        if (order.getOrderType() == OrderType.LIMIT &&
                (order.getPrice() == null ||
                 order.getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new InvalidOrderException(
                    "Limit orders require a positive price"
            );
        }

        if (order.getClientOrderId() == null ||
                order.getClientOrderId().isBlank()) {
            throw new InvalidOrderException(
                    "Client order ID is required"
            );
        }
    }
}
