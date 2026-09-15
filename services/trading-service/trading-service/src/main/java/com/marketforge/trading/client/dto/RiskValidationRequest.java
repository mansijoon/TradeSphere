package com.marketforge.trading.client.dto;

import com.marketforge.trading.domain.Order;

import java.math.BigDecimal;
import java.util.UUID;

public record RiskValidationRequest(
        UUID accountId,
        UUID instrumentId,
        String side,
        BigDecimal quantity,
        BigDecimal price
) {
    public static RiskValidationRequest from(Order order) {
        return new RiskValidationRequest(
                order.getAccountId(),
                order.getInstrumentId(),
                order.getSide().name(),
                order.getQuantity(),
                order.getPrice()
        );
    }
}
