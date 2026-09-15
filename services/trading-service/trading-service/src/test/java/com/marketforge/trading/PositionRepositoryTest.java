package com.marketforge.trading;

import com.marketforge.trading.domain.Position;
import com.marketforge.trading.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class PositionRepositoryTest {

    @Autowired
    private PositionRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void findByAccountAndInstrumentReturnsPosition() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.fromString(
                "00000000-0000-0000-0000-000000000001"
        );
        UUID positionId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users (id, username, email, password_hash)
            VALUES (?, ?, ?, ?)
            """,
            userId,
            "position_test_" + userId.toString().substring(0, 8),
            "position-test-" + userId + "@marketforge.local",
            "test-password-hash"
        );

        jdbc.update("""
            INSERT INTO trading_accounts
                (id, user_id, currency, cash_balance,
                 available_balance, reserved_balance, status)
            VALUES (?, ?, 'USD', 100000, 100000, 0, 'ACTIVE')
            """,
            accountId,
            userId
        );

        jdbc.update("""
            INSERT INTO positions
                (id, account_id, instrument_id, quantity,
                 average_entry_price, realized_pnl)
            VALUES (?, ?, ?, 25, 150.50, 42.75)
            """,
            positionId,
            accountId,
            instrumentId
        );

        Position position = repository
                .findByAccountIdAndInstrumentId(accountId, instrumentId)
                .orElseThrow();

        assertEquals(positionId, position.getId());
        assertEquals(accountId, position.getAccountId());
        assertEquals(instrumentId, position.getInstrumentId());
        assertEquals(new BigDecimal("25.00000000"), position.getQuantity());
        assertEquals(
                new BigDecimal("150.50000000"),
                position.getAverageEntryPrice()
        );
        assertEquals(
                new BigDecimal("42.75000000"),
                position.getRealizedPnl()
        );
    }
}
