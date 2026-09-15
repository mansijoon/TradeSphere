package com.marketforge.trading.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketforge.trading.domain.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OrderEventProducer {

    private static final String TOPIC = "order.commands";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(Order order) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();

            event.put("eventType", "OrderSubmitted");
            event.put("eventVersion", 1);
            event.put("orderId", order.getId());
            event.put("accountId", order.getAccountId());
            event.put("instrumentId", order.getInstrumentId());
            event.put("side", order.getSide());
            event.put("orderType", order.getOrderType());
            event.put("timeInForce", order.getTimeInForce());
            event.put("price", order.getPrice());
            event.put("quantity", order.getQuantity());
            event.put("timestamp", OffsetDateTime.now());

            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    TOPIC,
                    order.getId().toString(),
                    payload
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize order event",
                    e
            );
        }
    }
}
