package com.marketforge.trading.dto;

import com.marketforge.trading.domain.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID accountId,
        @NotNull UUID instrumentId,
        @NotBlank @Size(max = 64) String clientOrderId,
        @NotNull OrderSide side,
        @NotNull OrderType orderType,
        @NotNull TimeInForce timeInForce,
        @Positive BigDecimal price,
        @NotNull @Positive BigDecimal quantity
) {}
