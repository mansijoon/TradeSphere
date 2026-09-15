package com.marketforge.trading.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketforge.trading.domain.Order;
import com.marketforge.trading.domain.OrderSide;
import com.marketforge.trading.domain.Trade;
import com.marketforge.trading.repository.OrderRepository;
import com.marketforge.trading.repository.TradeRepository;
import com.marketforge.trading.repository.TradingAccountRepository;
import com.marketforge.trading.service.PositionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class TradeExecutedConsumer {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final TradingAccountRepository accountRepository;
    private final PositionService positionService;

    public TradeExecutedConsumer(
            ObjectMapper objectMapper,
            OrderRepository orderRepository,
            TradeRepository tradeRepository,
            TradingAccountRepository accountRepository,
            PositionService positionService
    ) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.accountRepository = accountRepository;
        this.positionService = positionService;
    }

    @KafkaListener(
            topics = "trade.executed",
            groupId = "trading-service-trades"
    )
    @Transactional
    public void consume(String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);

            validateEvent(event);

            String externalTradeId = requiredText(event, "tradeId");
            UUID instrumentId = UUID.fromString(
                    requiredText(event, "instrumentId")
            );
            UUID takerOrderId = UUID.fromString(
                    requiredText(event, "takerOrderId")
            );
            UUID makerOrderId = UUID.fromString(
                    requiredText(event, "makerOrderId")
            );

            BigDecimal price = requiredDecimal(event, "price");
            BigDecimal quantity = requiredDecimal(event, "quantity");
            OffsetDateTime executedAt = OffsetDateTime.parse(
                    requiredText(event, "timestamp")
            );

            if (tradeRepository
                    .findByInstrumentIdAndExternalTradeId(
                            instrumentId,
                            externalTradeId
                    )
                    .isPresent()) {
                return;
            }

            Order maker = orderRepository.findById(makerOrderId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Maker order not found: " + makerOrderId
                    ));

            Order taker = orderRepository.findById(takerOrderId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Taker order not found: " + takerOrderId
                    ));

            UUID buyOrderId;
            UUID sellOrderId;

            if (maker.getSide() == OrderSide.BUY) {
                buyOrderId = makerOrderId;
                sellOrderId = takerOrderId;
            } else {
                buyOrderId = takerOrderId;
                sellOrderId = makerOrderId;
            }

            tradeRepository.save(
                    new Trade(
                            instrumentId,
                            buyOrderId,
                            sellOrderId,
                            price,
                            quantity,
                            executedAt,
                            externalTradeId
                    )
            );

            maker.applyFill(quantity);
            taker.applyFill(quantity);

            orderRepository.save(maker);
            orderRepository.save(taker);

            positionService.applyFill(
                    maker.getAccountId(),
                    instrumentId,
                    maker.getSide(),
                    quantity,
                    price
            );

            positionService.applyFill(
                    taker.getAccountId(),
                    instrumentId,
                    taker.getSide(),
                    quantity,
                    price
            );

            if (buyOrderId != null) {
                Order buyOrder = maker.getSide() == OrderSide.BUY
                        ? maker
                        : taker;

                if (buyOrder.getPrice() != null) {
                    BigDecimal reservedAmount =
                            buyOrder.getPrice().multiply(quantity);

                    BigDecimal actualCost =
                            price.multiply(quantity);

                    int settled = accountRepository.settleBuyFill(
                            buyOrder.getAccountId(),
                            reservedAmount,
                            actualCost
                    );

                    if (settled != 1) {
                        throw new IllegalStateException(
                                "Unable to settle BUY fill for account: "
                                        + buyOrder.getAccountId()
                        );
                    }
                }
            }

            Order sellOrder = maker.getSide() == OrderSide.SELL
                    ? maker
                    : taker;

            BigDecimal proceeds = price.multiply(quantity);

            int sellerSettled = accountRepository.settleSellFill(
                    sellOrder.getAccountId(),
                    proceeds
            );

            if (sellerSettled != 1) {
                throw new IllegalStateException(
                        "Unable to settle SELL fill for account: "
                                + sellOrder.getAccountId()
                );
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to process TradeExecuted event",
                    e
            );
        }
    }

    private void validateEvent(JsonNode event) {
        if (!event.isObject()) {
            throw new IllegalArgumentException("Trade event must be an object");
        }

        if (!"TradeExecuted".equals(
                event.path("eventType").asText()
        )) {
            throw new IllegalArgumentException("Invalid eventType");
        }

        if (event.path("eventVersion").asInt(-1) != 1) {
            throw new IllegalArgumentException("Unsupported eventVersion");
        }
    }

    private String requiredText(JsonNode event, String field) {
        JsonNode node = event.get(field);

        if (node == null || !node.isTextual() || node.asText().isBlank()) {
            throw new IllegalArgumentException(
                    "Missing or invalid field: " + field
            );
        }

        return node.asText();
    }

    private BigDecimal requiredDecimal(JsonNode event, String field) {
        JsonNode node = event.get(field);

        if (node == null || !node.isNumber()
                || node.decimalValue().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Missing or invalid field: " + field
            );
        }

        return node.decimalValue();
    }
}
