package com.marketforge.trading;

import com.marketforge.trading.domain.Position;
import com.marketforge.trading.domain.TradingAccount;
import com.marketforge.trading.repository.PositionRepository;
import com.marketforge.trading.repository.TradingAccountRepository;
import com.marketforge.trading.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortfolioServiceTest {

    private TradingAccountRepository accountRepository;
    private PositionRepository positionRepository;
    private PortfolioService service;

    private UUID accountId;
    private UUID instrumentId;

    @BeforeEach
    void setUp() {
        accountRepository = mock(TradingAccountRepository.class);
        positionRepository = mock(PositionRepository.class);
        service = new PortfolioService(accountRepository, positionRepository);

        accountId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();
    }

    @Test
    void calculatesMarketValueAndUnrealizedPnl() {
        TradingAccount account = account(
                new BigDecimal("1000"),
                new BigDecimal("800")
        );

        Position position = position(
                new BigDecimal("10"),
                new BigDecimal("100"),
                new BigDecimal("25")
        );

        when(accountRepository.findById(accountId))
                .thenReturn(java.util.Optional.of(account));

        when(positionRepository.findByAccountIdAndInstrumentId(
                accountId,
                instrumentId
        )).thenReturn(java.util.Optional.of(position));

        var result = service.calculate(
                accountId,
                instrumentId,
                new BigDecimal("120")
        );

        assertEquals(
                0,
                new BigDecimal("1200").compareTo(result.marketValue())
        );

        assertEquals(
                0,
                new BigDecimal("200").compareTo(result.unrealizedPnl())
        );

        assertEquals(
                0,
                new BigDecimal("25").compareTo(result.realizedPnl())
        );

        assertEquals(
                0,
                new BigDecimal("2200").compareTo(result.totalEquity())
        );
    }

    @Test
    void zeroPositionHasZeroMarketValueAndUnrealizedPnl() {
        TradingAccount account = account(
                new BigDecimal("1000"),
                new BigDecimal("1000")
        );

        Position position = position(
                BigDecimal.ZERO,
                new BigDecimal("100"),
                new BigDecimal("50")
        );

        when(accountRepository.findById(accountId))
                .thenReturn(java.util.Optional.of(account));

        when(positionRepository.findByAccountIdAndInstrumentId(
                accountId,
                instrumentId
        )).thenReturn(java.util.Optional.of(position));

        var result = service.calculate(
                accountId,
                instrumentId,
                new BigDecimal("120")
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.marketValue())
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.unrealizedPnl())
        );

        assertEquals(
                0,
                new BigDecimal("50").compareTo(result.realizedPnl())
        );

        assertEquals(
                0,
                new BigDecimal("1000").compareTo(result.totalEquity())
        );
    }

    @Test
    void missingAccountIsRejected() {
        when(accountRepository.findById(accountId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.calculate(
                        accountId,
                        instrumentId,
                        new BigDecimal("100")
                )
        );
    }

    @Test
    void missingPositionUsesZeroPositionValue() {
        TradingAccount account = account(
                new BigDecimal("1000"),
                new BigDecimal("1000")
        );

        when(accountRepository.findById(accountId))
                .thenReturn(java.util.Optional.of(account));

        when(positionRepository.findByAccountIdAndInstrumentId(
                accountId,
                instrumentId
        )).thenReturn(java.util.Optional.empty());

        var result = service.calculate(
                accountId,
                instrumentId,
                new BigDecimal("100")
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.marketValue())
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.unrealizedPnl())
        );

        assertEquals(
                0,
                new BigDecimal("1000").compareTo(result.totalEquity())
        );
    }

    private TradingAccount account(
            BigDecimal cash,
            BigDecimal available
    ) {
        return mockAccount(cash, available);
    }

    private TradingAccount mockAccount(
            BigDecimal cash,
            BigDecimal available
    ) {
        TradingAccount account = mock(TradingAccount.class);
        when(account.getCashBalance()).thenReturn(cash);
        when(account.getAvailableBalance()).thenReturn(available);
        return account;
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
