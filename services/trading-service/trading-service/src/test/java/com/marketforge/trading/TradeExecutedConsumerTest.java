package com.marketforge.trading;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketforge.trading.domain.*;
import com.marketforge.trading.messaging.TradeExecutedConsumer;
import com.marketforge.trading.repository.OrderRepository;
import com.marketforge.trading.repository.TradeRepository;
import com.marketforge.trading.repository.TradingAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TradeExecutedConsumerTest {

    @Test
    void classCanBeConstructed() {
        TradeExecutedConsumer consumer =
                new TradeExecutedConsumer(
                        new ObjectMapper(),
                        null,
                        null,
                        null,
                        null
                );

        assertNotNull(consumer);
    }
}
