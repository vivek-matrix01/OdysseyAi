package com.OdysseyAi.demo.travel.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelContext {

    private String destination;

    @Builder.Default
    private List<PlaceData> places =
            new ArrayList<>();

    @Builder.Default
    private List<WeatherData> weather =
            new ArrayList<>();

    @Builder.Default
    private List<RouteData> routes =
            new ArrayList<>();

    private String baseCurrency;

    private String targetCurrency;

    private BigDecimal exchangeRate;
}