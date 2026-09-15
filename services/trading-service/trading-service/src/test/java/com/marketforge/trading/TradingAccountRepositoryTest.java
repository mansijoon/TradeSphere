package com.marketforge.trading;

import com.marketforge.trading.domain.AccountStatus;
import com.marketforge.trading.domain.TradingAccount;
import com.marketforge.trading.repository.TradingAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class TradingAccountRepositoryTest {

    @Autowired
    private TradingAccountRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void findByIdReturnsAccountWithBalances() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users (id, username, email, password_hash)
            VALUES (?, ?, ?, ?)
            """,
            userId,
            "account_test_" + userId.toString().substring(0, 8),
            "account-test-" + userId + "@marketforge.local",
            "test-password-hash"
        );

        jdbc.update("""
            INSERT INTO trading_accounts
                (id, user_id, currency, cash_balance,
                 available_balance, reserved_balance, status)
            VALUES (?, ?, 'USD', 100000, 75000, 25000, 'ACTIVE')
            """,
            accountId,
            userId
        );

        TradingAccount account = repository.findById(accountId).orElseThrow();

        assertEquals(accountId, account.getId());
        assertEquals(userId, account.getUserId());
        assertEquals("USD", account.getCurrency());
        assertEquals(new BigDecimal("100000.00000000"), account.getCashBalance());
        assertEquals(new BigDecimal("75000.00000000"), account.getAvailableBalance());
        assertEquals(new BigDecimal("25000.00000000"), account.getReservedBalance());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }
}
