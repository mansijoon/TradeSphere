package com.marketforge.market.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketforge.market.domain.MarketTick;
import com.marketforge.market.service.MarketDataService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class TradeExecutedConsumer {

    private final ObjectMapper objectMapper;
    private final MarketDataService marketDataService;

    public TradeExecutedConsumer(
            ObjectMapper objectMapper,
            MarketDataService marketDataService
    ) {
        this.objectMapper = objectMapper;
        this.marketDataService = marketDataService;
    }

    @KafkaListener(
            topics = "trade.executed",
            groupId = "market-data-service"
    )
    public void consume(String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);

            if (!"TradeExecuted".equals(event.path("eventType").asText())) {
                return;
            }

            if (event.path("eventVersion").asInt() != 1) {
                throw new IllegalArgumentException(
                        "Unsupported TradeExecuted event version"
                );
            }

            MarketTick tick = new MarketTick(
                    UUID.fromString(event.path("instrumentId").asText()),
                    event.path("price").decimalValue(),
                    event.path("quantity").decimalValue(),
                    OffsetDateTime.parse(
                            event.path("timestamp").asText()
                    )
            );

            marketDataService.record(tick);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to process TradeExecuted event",
                    e
            );
        }
    }
}
