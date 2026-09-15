package com.marketforge.trading.service;

import com.marketforge.trading.domain.Position;
import com.marketforge.trading.domain.TradingAccount;
import com.marketforge.trading.repository.PositionRepository;
import com.marketforge.trading.repository.TradingAccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PortfolioService {

    private final TradingAccountRepository accountRepository;
    private final PositionRepository positionRepository;

    public PortfolioService(
            TradingAccountRepository accountRepository,
            PositionRepository positionRepository
    ) {
        this.accountRepository = accountRepository;
        this.positionRepository = positionRepository;
    }

    public PortfolioResult calculate(
            UUID accountId,
            UUID instrumentId,
            BigDecimal currentMarketPrice
    ) {
        if (currentMarketPrice == null
                || currentMarketPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Current market price must be positive"
            );
        }

        TradingAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException(
                        "Trading account not found: " + accountId
                ));

        Position position = positionRepository
                .findByAccountIdAndInstrumentId(accountId, instrumentId)
                .orElse(null);

        BigDecimal quantity = position == null
                ? BigDecimal.ZERO
                : position.getQuantity();

        BigDecimal averageEntryPrice = position == null
                ? BigDecimal.ZERO
                : position.getAverageEntryPrice();

        BigDecimal realizedPnl = position == null
                ? BigDecimal.ZERO
                : position.getRealizedPnl();

        BigDecimal marketValue =
                quantity.multiply(currentMarketPrice);

        BigDecimal unrealizedPnl =
                currentMarketPrice
                        .subtract(averageEntryPrice)
                        .multiply(quantity);

        BigDecimal totalEquity =
                account.getCashBalance().add(marketValue);

        return new PortfolioResult(
                accountId,
                instrumentId,
                quantity,
                averageEntryPrice,
                account.getCashBalance(),
                account.getAvailableBalance(),
                marketValue,
                unrealizedPnl,
                realizedPnl,
                totalEquity
        );
    }

    public record PortfolioResult(
            UUID accountId,
            UUID instrumentId,
            BigDecimal quantity,
            BigDecimal averageEntryPrice,
            BigDecimal cash,
            BigDecimal availableBalance,
            BigDecimal marketValue,
            BigDecimal unrealizedPnl,
            BigDecimal realizedPnl,
            BigDecimal totalEquity
    ) {
    }
}
