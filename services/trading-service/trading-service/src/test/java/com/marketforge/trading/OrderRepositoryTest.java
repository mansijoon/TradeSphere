package com.marketforge.trading;

import com.marketforge.trading.domain.*;
import com.marketforge.trading.repository.OrderRepository;
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
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void findByAccountIdAndClientOrderIdReturnsOrder() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID instrumentId = UUID.fromString(
                "00000000-0000-0000-0000-000000000001"
        );

        jdbc.update("""
            INSERT INTO users (id, username, email, password_hash)
            VALUES (?, ?, ?, ?)
            """,
            userId,
            "order_test_" + userId.toString().substring(0, 8),
            "order-test-" + userId + "@marketforge.local",
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

        Order order = new Order(
                accountId,
                instrumentId,
                "CLIENT-001",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("100.00"),
                new BigDecimal("10")
        );

        repository.saveAndFlush(order);

        Order result = repository
                .findByAccountIdAndClientOrderId(accountId, "CLIENT-001")
                .orElseThrow();

        assertEquals("CLIENT-001", result.getClientOrderId());
        assertEquals(accountId, result.getAccountId());
    }
}
