package com.marketforge.trading.service;

import com.marketforge.trading.domain.OrderSide;
import com.marketforge.trading.domain.Position;
import com.marketforge.trading.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PositionService {

    private static final int SCALE = 8;

    private final PositionRepository repository;

    public PositionService(PositionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Position applyFill(
            UUID accountId,
            UUID instrumentId,
            OrderSide side,
            BigDecimal quantity,
            BigDecimal price
    ) {
        validateFill(quantity, price);

        Position position = repository
                .findByAccountIdAndInstrumentId(accountId, instrumentId)
                .orElse(null);

        if (side == OrderSide.BUY) {
            return applyBuy(
                    position,
                    accountId,
                    instrumentId,
                    quantity,
                    price
            );
        }

        if (side == OrderSide.SELL) {
            return applySell(position, quantity, price);
        }

        throw new IllegalArgumentException("Unsupported order side: " + side);
    }

    private Position applyBuy(
            Position position,
            UUID accountId,
            UUID instrumentId,
            BigDecimal quantity,
            BigDecimal price
    ) {
        if (position == null) {
            position = new Position(
                    UUID.randomUUID(),
                    accountId,
                    instrumentId,
                    quantity,
                    price,
                    BigDecimal.ZERO,
                    OffsetDateTime.now()
            );
        } else {
            BigDecimal oldQuantity = position.getQuantity();
            BigDecimal oldAverage = position.getAverageEntryPrice();

            BigDecimal newQuantity = oldQuantity.add(quantity);

            BigDecimal weightedCost = oldQuantity
                    .multiply(oldAverage)
                    .add(quantity.multiply(price));

            BigDecimal newAverage = weightedCost
                    .divide(newQuantity, SCALE, RoundingMode.HALF_UP);

            position = new Position(
                    position.getId(),
                    accountId,
                    instrumentId,
                    newQuantity,
                    newAverage,
                    position.getRealizedPnl(),
                    OffsetDateTime.now()
            );
        }

        return repository.save(position);
    }

    private Position applySell(
            Position position,
            BigDecimal quantity,
            BigDecimal price
    ) {
        if (position == null) {
            throw new IllegalStateException("No position available");
        }

        BigDecimal currentQuantity = position.getQuantity();

        if (quantity.compareTo(currentQuantity) > 0) {
            throw new IllegalStateException(
                    "Insufficient position: requested "
                            + quantity
                            + ", available "
                            + currentQuantity
            );
        }

        BigDecimal realizedPnl = price
                .subtract(position.getAverageEntryPrice())
                .multiply(quantity);

        BigDecimal newQuantity = currentQuantity.subtract(quantity);

        Position updated = new Position(
                position.getId(),
                position.getAccountId(),
                position.getInstrumentId(),
                newQuantity,
                position.getAverageEntryPrice(),
                position.getRealizedPnl().add(realizedPnl),
                OffsetDateTime.now()
        );

        return repository.save(updated);
    }

    private void validateFill(
            BigDecimal quantity,
            BigDecimal price
    ) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }
}
