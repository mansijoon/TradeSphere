package com.marketforge.trading;

import com.marketforge.trading.dto.CreateOrderRequest;
import com.marketforge.trading.client.RiskClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.marketforge.trading.config.KafkaTestConfiguration;
import com.marketforge.trading.domain.*;
import com.marketforge.trading.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.context.annotation.Import(KafkaTestConfiguration.class)
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private OrderRepository repository;

    @MockitoBean
    private RiskClient riskClient;

    @Test
    void createOrderReturnsCreatedOrder() throws Exception {
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
            "api_test_" + userId.toString().substring(0, 8),
            "api-test-" + userId + "@marketforge.local",
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

        String body = """
            {
              "accountId": "%s",
              "instrumentId": "%s",
              "clientOrderId": "API-TEST-001",
              "side": "BUY",
              "orderType": "LIMIT",
              "timeInForce": "DAY",
              "price": 100.00,
              "quantity": 10
            }
            """.formatted(accountId, instrumentId);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientOrderId").value("API-TEST-001"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }
}
