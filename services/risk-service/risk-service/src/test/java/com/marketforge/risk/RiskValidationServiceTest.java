package com.marketforge.risk;

import com.marketforge.risk.domain.RiskAccount;
import com.marketforge.risk.dto.RiskValidationRequest;
import com.marketforge.risk.exception.RiskRejectedException;
import com.marketforge.risk.repository.RiskRepository;
import com.marketforge.risk.service.RiskValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RiskValidationServiceTest {

    private RiskRepository repository;
    private RiskValidationService service;

    private UUID accountId;
    private UUID instrumentId;

    @BeforeEach
    void setUp() {
        repository = mock(RiskRepository.class);
        service = new RiskValidationService(repository);

        accountId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();
    }

    @Test
    void buyWithinBuyingPowerIsApproved() {
        when(repository.findAccount(accountId))
                .thenReturn(Optional.of(
                        new RiskAccount(
                                "ACTIVE",
                                new BigDecimal("10000")
                        )
                ));

        RiskValidationRequest request = new RiskValidationRequest(
                accountId,
                instrumentId,
                "BUY",
                new BigDecimal("10"),
                new BigDecimal("100")
        );

        assertDoesNotThrow(() -> service.validate(request));
    }

    @Test
    void buyExceedingBuyingPowerIsRejected() {
        when(repository.findAccount(accountId))
                .thenReturn(Optional.of(
                        new RiskAccount(
                                "ACTIVE",
                                new BigDecimal("500")
                        )
                ));

        RiskValidationRequest request = new RiskValidationRequest(
                accountId,
                instrumentId,
                "BUY",
                new BigDecimal("10"),
                new BigDecimal("100")
        );

        RiskRejectedException exception = assertThrows(
                RiskRejectedException.class,
                () -> service.validate(request)
        );

        assertTrue(exception.getMessage().contains("Insufficient buying power"));
    }

    @Test
    void missingAccountIsRejected() {
        when(repository.findAccount(accountId))
                .thenReturn(Optional.empty());

        RiskValidationRequest request = new RiskValidationRequest(
                accountId,
                instrumentId,
                "BUY",
                new BigDecimal("1"),
                new BigDecimal("100")
        );

        assertThrows(
                RiskRejectedException.class,
                () -> service.validate(request)
        );
    }

    @Test
    void inactiveAccountIsRejected() {
        when(repository.findAccount(accountId))
                .thenReturn(Optional.of(
                        new RiskAccount(
                                "SUSPENDED",
                                new BigDecimal("10000")
                        )
                ));

        RiskValidationRequest request = new RiskValidationRequest(
                accountId,
                instrumentId,
                "BUY",
                new BigDecimal("1"),
                new BigDecimal("100")
        );

        RiskRejectedException exception = assertThrows(
                RiskRejectedException.class,
                () -> service.validate(request)
        );

        assertEquals(
                "Trading account is not active",
                exception.getMessage()
        );
    }

    @Test
    void sellWithinPositionIsApproved() {
        when(repository.findAccount(accountId))
                .thenReturn(Optional.of(
                        new RiskAccount(
                                "ACTIVE",
                                new BigDecimal("10000")
                        )
                ));

        when(repository.findPositionQuantity(accountId, instrumentId))
                .thenReturn(Optional.of(new BigDecimal("10")));

        RiskValidationRequest request = new RiskValidationRequest(
                accountId,
                instrumentId,
                "SELL",
                new BigDecimal("5"),
                new BigDecimal("100")
        );

        assertDoesNotThrow(() -> service.validate(request));
    }

    @Test
    void sellExceedingPositionIsRejected() {
        when(repository.findAccount(accountId))
                .thenReturn(Optional.of(
                        new RiskAccount(
                                "ACTIVE",
                                new BigDecimal("10000")
                        )
                ));

        when(repository.findPositionQuantity(accountId, instrumentId))
                .thenReturn(Optional.of(new BigDecimal("3")));

        RiskValidationRequest request = new RiskValidationRequest(
                accountId,
                instrumentId,
                "SELL",
                new BigDecimal("5"),
                new BigDecimal("100")
        );

        RiskRejectedException exception = assertThrows(
                RiskRejectedException.class,
                () -> service.validate(request)
        );

        assertTrue(exception.getMessage().contains("Insufficient position"));
    }

    @Test
    void sellWithoutPositionIsRejected() {
        when(repository.findAccount(accountId))
                .thenReturn(Optional.of(
                        new RiskAccount(
                                "ACTIVE",
                                new BigDecimal("10000")
                        )
                ));

        when(repository.findPositionQuantity(accountId, instrumentId))
                .thenReturn(Optional.empty());

        RiskValidationRequest request = new RiskValidationRequest(
                accountId,
                instrumentId,
                "SELL",
                new BigDecimal("1"),
                new BigDecimal("100")
        );

        assertThrows(
                RiskRejectedException.class,
                () -> service.validate(request)
        );
    }

    @Test
    void marketBuyDoesNotRequireBuyingPowerCalculation() {
        when(repository.findAccount(accountId))
                .thenReturn(Optional.of(
                        new RiskAccount(
                                "ACTIVE",
                                new BigDecimal("0")
                        )
                ));

        RiskValidationRequest request = new RiskValidationRequest(
                accountId,
                instrumentId,
                "BUY",
                new BigDecimal("10"),
                null
        );

        assertDoesNotThrow(() -> service.validate(request));
    }
}
