package com.marketforge.trading;

import com.marketforge.trading.repository.TradingAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class TradingAccountReservationTest {

    @Autowired
    private TradingAccountRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void reserveBuyingPowerMovesBalanceAtomically() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users (id, username, email, password_hash)
            VALUES (?, ?, ?, ?)
            """,
            userId,
            "reserve_test_" + userId.toString().substring(0, 8),
            "reserve-test-" + userId + "@marketforge.local",
            "test-password-hash"
        );

        jdbc.update("""
            INSERT INTO trading_accounts
                (id, user_id, currency, cash_balance,
                 available_balance, reserved_balance, status)
            VALUES (?, ?, 'USD', 100000, 1000, 0, 'ACTIVE')
            """,
            accountId,
            userId
        );

        int updated = repository.reserveBuyingPower(
                accountId,
                new BigDecimal("400")
        );

        assertEquals(1, updated);

        BigDecimal available = jdbc.queryForObject(
                "SELECT available_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                accountId
        );

        BigDecimal reserved = jdbc.queryForObject(
                "SELECT reserved_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                accountId
        );

        assertEquals(new BigDecimal("600.00000000"), available);
        assertEquals(new BigDecimal("400.00000000"), reserved);
    }

    @Test
    void insufficientBuyingPowerDoesNotChangeBalance() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users (id, username, email, password_hash)
            VALUES (?, ?, ?, ?)
            """,
            userId,
            "reserve_fail_" + userId.toString().substring(0, 8),
            "reserve-fail-" + userId + "@marketforge.local",
            "test-password-hash"
        );

        jdbc.update("""
            INSERT INTO trading_accounts
                (id, user_id, currency, cash_balance,
                 available_balance, reserved_balance, status)
            VALUES (?, ?, 'USD', 100000, 500, 0, 'ACTIVE')
            """,
            accountId,
            userId
        );

        int updated = repository.reserveBuyingPower(
                accountId,
                new BigDecimal("600")
        );

        assertEquals(0, updated);

        BigDecimal available = jdbc.queryForObject(
                "SELECT available_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                accountId
        );

        BigDecimal reserved = jdbc.queryForObject(
                "SELECT reserved_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                accountId
        );

        assertEquals(new BigDecimal("500.00000000"), available);
        assertEquals(new BigDecimal("0E-8"), reserved);
    }

    @Test
    void settleBuyFillReleasesPriceImprovement() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users (id, username, email, password_hash)
            VALUES (?, ?, ?, ?)
            """,
            userId,
            "settle_test_" + userId.toString().substring(0, 8),
            "settle-test-" + userId + "@marketforge.local",
            "test-password-hash"
        );

        jdbc.update("""
            INSERT INTO trading_accounts
                (id, user_id, currency, cash_balance,
                 available_balance, reserved_balance, status)
            VALUES (?, ?, 'USD', 100000, 0, 1050, 'ACTIVE')
            """,
            accountId,
            userId
        );

        int updated = repository.settleBuyFill(
                accountId,
                new BigDecimal("1050"),
                new BigDecimal("1000")
        );

        assertEquals(1, updated);

        BigDecimal cash = jdbc.queryForObject(
                "SELECT cash_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                accountId
        );

        BigDecimal available = jdbc.queryForObject(
                "SELECT available_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                accountId
        );

        BigDecimal reserved = jdbc.queryForObject(
                "SELECT reserved_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                accountId
        );

        assertEquals(new BigDecimal("99000.00000000"), cash);
        assertEquals(new BigDecimal("50.00000000"), available);
        assertEquals(new BigDecimal("0E-8"), reserved);
    }
}

