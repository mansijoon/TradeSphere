package com.marketforge.risk.domain;

import java.math.BigDecimal;

public record RiskAccount(
        String status,
        BigDecimal availableBalance
) {
}
