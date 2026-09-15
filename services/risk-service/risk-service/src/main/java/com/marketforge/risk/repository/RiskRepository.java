package com.marketforge.risk.repository;

import com.marketforge.risk.domain.RiskAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RiskRepository {

    private final JdbcTemplate jdbcTemplate;

    public RiskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<RiskAccount> findAccount(UUID accountId) {
        return jdbcTemplate.query(
                """
                SELECT status, available_balance
                FROM trading_accounts
                WHERE id = ?
                """,
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    return Optional.of(new RiskAccount(
                            rs.getString("status"),
                            rs.getBigDecimal("available_balance")
                    ));
                },
                accountId
        );
    }

    public Optional<BigDecimal> findPositionQuantity(
            UUID accountId,
            UUID instrumentId
    ) {
        return jdbcTemplate.query(
                """
                SELECT quantity
                FROM positions
                WHERE account_id = ?
                  AND instrument_id = ?
                """,
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    return Optional.ofNullable(
                            rs.getBigDecimal("quantity")
                    );
                },
                accountId,
                instrumentId
        );
    }
}
