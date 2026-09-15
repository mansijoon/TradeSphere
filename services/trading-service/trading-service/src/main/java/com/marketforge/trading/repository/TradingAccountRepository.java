package com.marketforge.trading.repository;

import com.marketforge.trading.domain.TradingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public interface TradingAccountRepository extends JpaRepository<TradingAccount, UUID> {

    @Modifying
    @Query(value = """
        UPDATE trading_accounts
        SET available_balance = available_balance - :amount,
            reserved_balance = reserved_balance + :amount,
            updated_at = NOW()
        WHERE id = :accountId
          AND status = 'ACTIVE'::account_status
          AND available_balance >= :amount
        """, nativeQuery = true)
    int reserveBuyingPower(
            @Param("accountId") UUID accountId,
            @Param("amount") BigDecimal amount
    );

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE trading_accounts
        SET cash_balance = cash_balance - :actualCost,
            available_balance = available_balance
                + :reservedAmount - :actualCost,
            reserved_balance = reserved_balance - :reservedAmount,
            updated_at = NOW()
        WHERE id = :accountId
          AND reserved_balance >= :reservedAmount
          AND cash_balance >= :actualCost
        """, nativeQuery = true)
    int settleBuyFill(
            @Param("accountId") UUID accountId,
            @Param("reservedAmount") BigDecimal reservedAmount,
            @Param("actualCost") BigDecimal actualCost
    );

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE trading_accounts
        SET cash_balance = cash_balance + :proceeds,
            available_balance = available_balance + :proceeds,
            updated_at = NOW()
        WHERE id = :accountId
          AND status = 'ACTIVE'::account_status
        """, nativeQuery = true)
    int settleSellFill(
            @Param("accountId") UUID accountId,
            @Param("proceeds") BigDecimal proceeds
    );
}
