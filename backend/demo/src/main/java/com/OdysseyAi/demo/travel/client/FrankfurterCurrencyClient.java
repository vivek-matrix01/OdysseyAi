package com.OdysseyAi.demo.travel.client;

import com.OdysseyAi.demo.travel.interfaces.CurrencyClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class FrankfurterCurrencyClient
        implements CurrencyClient {

    private final ObjectMapper objectMapper;

    private final RestClient restClient =
            RestClient.builder()
                    .baseUrl("https://api.frankfurter.dev/v2")
                    .build();

    public FrankfurterCurrencyClient(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public BigDecimal getExchangeRate(
            String baseCurrency,
            String targetCurrency
    ) {

        if (baseCurrency.equalsIgnoreCase(targetCurrency)) {
            return BigDecimal.ONE;
        }

        String response =
                restClient.get()
                        .uri(
                                "/rate/{base}/{quote}",
                                baseCurrency.toUpperCase(),
                                targetCurrency.toUpperCase()
                        )
                        .retrieve()
                        .body(String.class);

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            return root.path("rate")
                    .decimalValue();

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to parse Frankfurter response.",
                    ex
            );
        }
    }
}