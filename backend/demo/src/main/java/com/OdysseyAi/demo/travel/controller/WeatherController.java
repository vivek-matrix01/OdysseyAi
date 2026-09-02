package com.OdysseyAi.demo.travel.controller;

import com.OdysseyAi.demo.common.api.ApiResponse;
import com.OdysseyAi.demo.travel.dto.WeatherData;
import com.OdysseyAi.demo.travel.dto.WeatherResponse;
import com.OdysseyAi.demo.travel.interfaces.WeatherService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/travel/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<ApiResponse<WeatherResponse>> getWeather(
            @RequestParam
            @NotBlank
            String destination,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {

        List<WeatherData> forecast =
                weatherService.getWeather(
                        destination,
                        startDate,
                        endDate
                );

        boolean forecastAvailable =
                !forecast.isEmpty();

        String message =
                forecastAvailable
                        ? "Weather forecast retrieved successfully."
                        : "Weather forecast is not currently available for the selected dates.";

        WeatherResponse response =
                WeatherResponse.builder()
                        .destination(destination)
                        .startDate(startDate)
                        .endDate(endDate)
                        .forecastAvailable(
                                forecastAvailable
                        )
                        .dailyForecast(forecast)
                        .message(message)
                        .build();

        return ResponseEntity.ok(
                ApiResponse.<WeatherResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .data(response)
                        .build()
        );
    }
}