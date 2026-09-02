package com.OdysseyAi.demo.travel.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherData {

    private LocalDate date;

    private Double temperatureMax;

    private Double temperatureMin;

    private Double precipitation;

    private Double precipitationProbability;

    private Double windSpeed;

    private Integer weatherCode;

    private String condition;

    private Boolean rainExpected;
}