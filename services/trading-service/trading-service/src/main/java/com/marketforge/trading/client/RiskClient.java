package com.marketforge.trading.client;

import com.marketforge.trading.client.dto.RiskValidationRequest;
import com.marketforge.trading.client.dto.RiskValidationResponse;
import com.marketforge.trading.domain.Order;
import com.marketforge.trading.exception.InvalidOrderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RiskClient {

    private final RestClient restClient;

    public RiskClient(
            RestClient.Builder restClientBuilder,
            @Value("${risk.service.url:http://localhost:8081}") String riskServiceUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(riskServiceUrl)
                .build();
    }

    public void validate(Order order) {
        try {
            RiskValidationResponse response = restClient
                    .post()
                    .uri("/api/v1/risk/validate")
                    .body(RiskValidationRequest.from(order))
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (request, httpResponse) -> {
                                throw new InvalidOrderException(
                                        "Risk validation rejected order"
                                );
                            }
                    )
                    .body(RiskValidationResponse.class);

            if (response == null || !response.approved()) {
                throw new InvalidOrderException(
                        "Risk validation rejected order"
                );
            }

        } catch (InvalidOrderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidOrderException(
                    "Risk service unavailable"
            );
        }
    }
}
