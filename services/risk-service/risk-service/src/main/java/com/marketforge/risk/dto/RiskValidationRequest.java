package com.marketforge.risk.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RiskValidationRequest(
        @NotNull UUID accountId,
        @NotNull UUID instrumentId,
        @NotNull String side,
        @NotNull BigDecimal quantity,
        BigDecimal price
) {
}
