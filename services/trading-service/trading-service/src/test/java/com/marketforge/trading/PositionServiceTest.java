package com.marketforge.trading;

import com.marketforge.trading.domain.OrderSide;
import com.marketforge.trading.domain.Position;
import com.marketforge.trading.repository.PositionRepository;
import com.marketforge.trading.service.PositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PositionServiceTest {

    private PositionRepository repository;
    private PositionService service;

    private UUID accountId;
    private UUID instrumentId;

    @BeforeEach
    void setUp() {
        repository = mock(PositionRepository.class);
        service = new PositionService(repository);

        accountId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();
    }

    @Test
    void buyCreatesNewPosition() {
        when(repository.findByAccountIdAndInstrumentId(accountId, instrumentId))
                .thenReturn(Optional.empty());

        service.applyFill(
                accountId,
                instrumentId,
                OrderSide.BUY,
                new BigDecimal("10"),
                new BigDecimal("100")
        );

        verify(repository).save(argThat(position ->
                position.getAccountId().equals(accountId)
                        && position.getInstrumentId().equals(instrumentId)
                        && position.getQuantity().compareTo(
                                new BigDecimal("10")) == 0
                        && position.getAverageEntryPrice().compareTo(
                                new BigDecimal("100")) == 0
                        && position.getRealizedPnl().compareTo(
                                BigDecimal.ZERO) == 0
        ));
    }

    @Test
    void secondBuyCalculatesWeightedAveragePrice() {
        Position existing = position(
                new BigDecimal("10"),
                new BigDecimal("100"),
                BigDecimal.ZERO
        );

        when(repository.findByAccountIdAndInstrumentId(accountId, instrumentId))
                .thenReturn(Optional.of(existing));

        service.applyFill(
                accountId,
                instrumentId,
                OrderSide.BUY,
                new BigDecimal("10"),
                new BigDecimal("110")
        );

        verify(repository).save(argThat(position ->
                position.getQuantity().compareTo(
                        new BigDecimal("20")) == 0
                        && position.getAverageEntryPrice().compareTo(
                        new BigDecimal("105")) == 0
                        && position.getRealizedPnl().compareTo(
                        BigDecimal.ZERO) == 0
        ));
    }

    @Test
    void sellReducesPositionAndRealizesProfit() {
        Position existing = position(
                new BigDecimal("20"),
                new BigDecimal("100"),
                BigDecimal.ZERO
        );

        when(repository.findByAccountIdAndInstrumentId(accountId, instrumentId))
                .thenReturn(Optional.of(existing));

        service.applyFill(
                accountId,
                instrumentId,
                OrderSide.SELL,
                new BigDecimal("5"),
                new BigDecimal("120")
        );

        verify(repository).save(argThat(position ->
                position.getQuantity().compareTo(
                        new BigDecimal("15")) == 0
                        && position.getAverageEntryPrice().compareTo(
                        new BigDecimal("100")) == 0
                        && position.getRealizedPnl().compareTo(
                        new BigDecimal("100")) == 0
        ));
    }

    @Test
    void sellClosingPositionLeavesZeroQuantity() {
        Position existing = position(
                new BigDecimal("10"),
                new BigDecimal("100"),
                BigDecimal.ZERO
        );

        when(repository.findByAccountIdAndInstrumentId(accountId, instrumentId))
                .thenReturn(Optional.of(existing));

        service.applyFill(
                accountId,
                instrumentId,
                OrderSide.SELL,
                new BigDecimal("10"),
                new BigDecimal("90")
        );

        verify(repository).save(argThat(position ->
                position.getQuantity().compareTo(BigDecimal.ZERO) == 0
                        && position.getAverageEntryPrice().compareTo(
                        new BigDecimal("100")) == 0
                        && position.getRealizedPnl().compareTo(
                        new BigDecimal("-100")) == 0
        ));
    }

    @Test
    void sellWithoutPositionIsRejected() {
        when(repository.findByAccountIdAndInstrumentId(accountId, instrumentId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.applyFill(
                        accountId,
                        instrumentId,
                        OrderSide.SELL,
                        new BigDecimal("5"),
                        new BigDecimal("100")
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void sellGreaterThanPositionIsRejected() {
        Position existing = position(
                new BigDecimal("5"),
                new BigDecimal("100"),
                BigDecimal.ZERO
        );

        when(repository.findByAccountIdAndInstrumentId(accountId, instrumentId))
                .thenReturn(Optional.of(existing));

        assertThrows(
                IllegalStateException.class,
                () -> service.applyFill(
                        accountId,
                        instrumentId,
                        OrderSide.SELL,
                        new BigDecimal("10"),
                        new BigDecimal("120")
                )
        );

        verify(repository, never()).save(any());
    }

    private Position position(
            BigDecimal quantity,
            BigDecimal averagePrice,
            BigDecimal realizedPnl
    ) {
        return new Position(
                UUID.randomUUID(),
                accountId,
                instrumentId,
                quantity,
                averagePrice,
                realizedPnl,
                java.time.OffsetDateTime.now()
        );
    }
}
