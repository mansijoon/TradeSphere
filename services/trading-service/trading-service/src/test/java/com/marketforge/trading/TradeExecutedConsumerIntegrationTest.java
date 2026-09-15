package com.marketforge.trading;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketforge.trading.domain.*;
import com.marketforge.trading.messaging.TradeExecutedConsumer;
import com.marketforge.trading.repository.OrderRepository;
import com.marketforge.trading.repository.TradeRepository;
import com.marketforge.trading.repository.TradingAccountRepository;
import com.marketforge.trading.service.PositionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(com.marketforge.trading.config.KafkaTestConfiguration.class)
@Transactional
class TradeExecutedConsumerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TradingAccountRepository accountRepository;
    
    @Autowired
    private PositionService positionService;

    @Test
    void tradeExecutedIsPersistedAndOrdersAreFilled() throws Exception {
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
            "trade_test_" + userId.toString().substring(0, 8),
            "trade-test-" + userId + "@marketforge.local",
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

        Order sell = new Order(
                accountId,
                instrumentId,
                "SELL-001",
                OrderSide.SELL,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("101.00"),
                new BigDecimal("10.00")
        );

        Order buy = new Order(
                accountId,
                instrumentId,
                "BUY-001",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("101.00"),
                new BigDecimal("10.00")
        );

        orderRepository.saveAndFlush(sell);
        orderRepository.saveAndFlush(buy);

        positionService.applyFill(
                accountId,
                instrumentId,
                OrderSide.BUY,
                new BigDecimal("10.00"),
                new BigDecimal("100.00")
        );

        int reserved = accountRepository.reserveBuyingPower(
                accountId,
                new BigDecimal("1010.00")
        );
        assertEquals(1, reserved);

        TradeExecutedConsumer consumer =
                new TradeExecutedConsumer(
                        objectMapper,
                        orderRepository,
                        tradeRepository,
                        accountRepository,
                        positionService
                );

        String tradeId = "integration-trade-" + UUID.randomUUID();

        String payload = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "eventType", "TradeExecuted",
                        "eventVersion", 1,
                        "tradeId", tradeId,
                        "takerOrderId", buy.getId().toString(),
                        "makerOrderId", sell.getId().toString(),
                        "instrumentId", instrumentId.toString(),
                        "price", 101.00,
                        "quantity", 10.00,
                        "timestamp", "2026-09-14T10:00:00Z"
                )
        );

        consumer.consume(payload);

        var trade = tradeRepository
                .findByInstrumentIdAndExternalTradeId(
                        instrumentId,
                        tradeId
                )
                .orElseThrow();

        assertEquals(instrumentId, trade.getInstrumentId());
        assertEquals(buy.getId(), trade.getBuyOrderId());
        assertEquals(sell.getId(), trade.getSellOrderId());
        assertEquals(
                0,
                new BigDecimal("101.00").compareTo(trade.getPrice())
        );
        assertEquals(
                0,
                new BigDecimal("10.00").compareTo(trade.getQuantity())
        );

        Order updatedBuy = orderRepository.findById(buy.getId()).orElseThrow();
        Order updatedSell = orderRepository.findById(sell.getId()).orElseThrow();

        assertEquals(OrderStatus.FILLED, updatedBuy.getStatus());
        assertEquals(OrderStatus.FILLED, updatedSell.getStatus());
    }

    @Test
    void sellerCashIsCreditedWhenTradeExecutes() throws Exception {
        UUID sellerUserId = UUID.randomUUID();
        UUID sellerAccountId = UUID.randomUUID();
        UUID buyerUserId = UUID.randomUUID();
        UUID buyerAccountId = UUID.randomUUID();

        UUID instrumentId = UUID.fromString(
                "00000000-0000-0000-0000-000000000001"
        );

        jdbc.update("""
            INSERT INTO users (id, username, email, password_hash)
            VALUES (?, ?, ?, ?), (?, ?, ?, ?)
            """,
            sellerUserId,
            "seller_settle_" + sellerUserId.toString().substring(0, 8),
            "seller-settle-" + sellerUserId + "@marketforge.local",
            "test-password-hash",
            buyerUserId,
            "buyer_settle_" + buyerUserId.toString().substring(0, 8),
            "buyer-settle-" + buyerUserId + "@marketforge.local",
            "test-password-hash"
        );

        jdbc.update("""
            INSERT INTO trading_accounts
                (id, user_id, currency, cash_balance,
                 available_balance, reserved_balance, status)
            VALUES (?, ?, 'USD', 100000, 100000, 0, 'ACTIVE'),
                   (?, ?, 'USD', 100000, 100000, 0, 'ACTIVE')
            """,
            sellerAccountId,
            sellerUserId,
            buyerAccountId,
            buyerUserId
        );

        Order sell = new Order(
                sellerAccountId,
                instrumentId,
                "SETTLE-SELL",
                OrderSide.SELL,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("106.00"),
                new BigDecimal("1.00")
        );

        Order buy = new Order(
                buyerAccountId,
                instrumentId,
                "SETTLE-BUY",
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("106.00"),
                new BigDecimal("1.00")
        );

        orderRepository.saveAndFlush(sell);
        orderRepository.saveAndFlush(buy);

        positionService.applyFill(
                sellerAccountId,
                instrumentId,
                OrderSide.BUY,
                new BigDecimal("1.00"),
                new BigDecimal("100.00")
        );

        assertEquals(
                1,
                accountRepository.reserveBuyingPower(
                        buyerAccountId,
                        new BigDecimal("106.00")
                )
        );

        TradeExecutedConsumer consumer =
                new TradeExecutedConsumer(
                        objectMapper,
                        orderRepository,
                        tradeRepository,
                        accountRepository,
                        positionService
                );

        String tradeId = "seller-settlement-" + UUID.randomUUID();

        String payload = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "eventType", "TradeExecuted",
                        "eventVersion", 1,
                        "tradeId", tradeId,
                        "takerOrderId", buy.getId().toString(),
                        "makerOrderId", sell.getId().toString(),
                        "instrumentId", instrumentId.toString(),
                        "price", 106.00,
                        "quantity", 1.00,
                        "timestamp", "2026-09-14T10:00:00Z"
                )
        );

        consumer.consume(payload);

        BigDecimal sellerCash = jdbc.queryForObject(
                "SELECT cash_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                sellerAccountId
        );

        BigDecimal sellerAvailable = jdbc.queryForObject(
                "SELECT available_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                sellerAccountId
        );

        BigDecimal buyerCash = jdbc.queryForObject(
                "SELECT cash_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                buyerAccountId
        );

        BigDecimal buyerAvailable = jdbc.queryForObject(
                "SELECT available_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                buyerAccountId
        );

        BigDecimal buyerReserved = jdbc.queryForObject(
                "SELECT reserved_balance FROM trading_accounts WHERE id = ?",
                BigDecimal.class,
                buyerAccountId
        );

        assertEquals(0, new BigDecimal("100106.00").compareTo(sellerCash));
        assertEquals(0, new BigDecimal("100106.00").compareTo(sellerAvailable));
        assertEquals(0, new BigDecimal("99894.00").compareTo(buyerCash));
        assertEquals(0, new BigDecimal("99894.00").compareTo(buyerAvailable));
        assertEquals(0, BigDecimal.ZERO.compareTo(buyerReserved));
    }

    @Test
    void duplicateTradeEventIsIgnored() throws Exception {
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
            "dup_trade_" + userId.toString().substring(0, 8),
            "dup-trade-" + userId + "@marketforge.local",
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

        Order sell = new Order(
                accountId, instrumentId, "DUP-SELL",
                OrderSide.SELL, OrderType.LIMIT, TimeInForce.DAY,
                new BigDecimal("101.00"), new BigDecimal("10.00")
        );

        Order buy = new Order(
                accountId, instrumentId, "DUP-BUY",
                OrderSide.BUY, OrderType.LIMIT, TimeInForce.DAY,
                new BigDecimal("101.00"), new BigDecimal("10.00")
        );

        orderRepository.saveAndFlush(sell);
        orderRepository.saveAndFlush(buy);

        positionService.applyFill(
                accountId,
                instrumentId,
                OrderSide.BUY,
                new BigDecimal("10.00"),
                new BigDecimal("100.00")
        );

        int reserved = accountRepository.reserveBuyingPower(
                accountId,
                new BigDecimal("1010.00")
        );
        assertEquals(1, reserved);

        TradeExecutedConsumer consumer =
                new TradeExecutedConsumer(
                        objectMapper,
                        orderRepository,
                        tradeRepository,
                        accountRepository,
                        positionService
                );

        String tradeId = "duplicate-trade-" + UUID.randomUUID();

        String payload = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "eventType", "TradeExecuted",
                        "eventVersion", 1,
                        "tradeId", tradeId,
                        "takerOrderId", buy.getId().toString(),
                        "makerOrderId", sell.getId().toString(),
                        "instrumentId", instrumentId.toString(),
                        "price", 101.00,
                        "quantity", 10.00,
                        "timestamp", "2026-09-14T10:00:00Z"
                )
        );

        consumer.consume(payload);
        consumer.consume(payload);

        assertEquals(
                1,
                tradeRepository.findByInstrumentIdAndExternalTradeId(
                        instrumentId,
                        tradeId
                ).stream().count()
        );
    }
}
