package com.OdysseyAi.demo.travel.interfaces;

import com.OdysseyAi.demo.travel.dto.WeatherData;

import java.time.LocalDate;
import java.util.List;

public interface WeatherService {

    List<WeatherData> getWeather(
            String destination,
            LocalDate startDate,
            LocalDate endDate
    );
}