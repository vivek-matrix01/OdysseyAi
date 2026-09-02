package com.OdysseyAi.demo.travel.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponse {

    private String destination;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean forecastAvailable;

    @Builder.Default
    private List<WeatherData> dailyForecast =
            new ArrayList<>();

    private String message;
}