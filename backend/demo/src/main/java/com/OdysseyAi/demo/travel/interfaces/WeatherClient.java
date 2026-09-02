package com.OdysseyAi.demo.travel.interfaces;

import com.OdysseyAi.demo.travel.dto.WeatherData;

import java.time.LocalDate;
import java.util.List;

public interface WeatherClient {

    List<WeatherData> getDailyForecast(
            double latitude,
            double longitude,
            LocalDate startDate,
            LocalDate endDate
    );
}