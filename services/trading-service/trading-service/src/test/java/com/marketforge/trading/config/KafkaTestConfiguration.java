package com.marketforge.trading.config;

import com.marketforge.trading.messaging.OrderEventProducer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class KafkaTestConfiguration {

    @Bean
    OrderEventProducer orderEventProducer() {
        return new OrderEventProducer(null, null) {
            @Override
            public void publish(com.marketforge.trading.domain.Order order) {
                // No-op for database/API integration tests.
            }
        };
    }
}
