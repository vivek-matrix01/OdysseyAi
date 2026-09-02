package com.OdysseyAi.demo.travel.client;

import com.OdysseyAi.demo.travel.dto.WeatherData;
import com.OdysseyAi.demo.travel.interfaces.WeatherClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenMeteoWeatherClient implements WeatherClient {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenMeteoWeatherClient(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        this.restClient =
                RestClient.builder().build();
    }

    @Override
    public List<WeatherData> getDailyForecast(
            double latitude,
            double longitude,
            LocalDate startDate,
            LocalDate endDate
    ) {

        LocalDate today =
                LocalDate.now();

        LocalDate latestForecastDate =
                today.plusDays(16);

        /*
         * No forecast available for the requested dates.
         */
        if (startDate.isAfter(latestForecastDate)) {
            return new ArrayList<>();
        }

        LocalDate effectiveStartDate =
                startDate.isBefore(today)
                        ? today
                        : startDate;

        LocalDate effectiveEndDate =
                endDate.isAfter(latestForecastDate)
                        ? latestForecastDate
                        : endDate;

        if (effectiveStartDate.isAfter(effectiveEndDate)) {
            return new ArrayList<>();
        }

        String response =
                restClient.get()
                        .uri(uriBuilder ->
                                uriBuilder
                                        .scheme("https")
                                        .host("api.open-meteo.com")
                                        .path("/v1/forecast")
                                        .queryParam(
                                                "latitude",
                                                latitude
                                        )
                                        .queryParam(
                                                "longitude",
                                                longitude
                                        )
                                        .queryParam(
                                                "daily",
                                                "temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,wind_speed_10m_max,weather_code"
                                        )
                                        .queryParam(
                                                "start_date",
                                                effectiveStartDate
                                        )
                                        .queryParam(
                                                "end_date",
                                                effectiveEndDate
                                        )
                                        .queryParam(
                                                "timezone",
                                                "auto"
                                        )
                                        .build()
                        )
                        .retrieve()
                        .body(String.class);

        return parseForecast(response);
    }

    private List<WeatherData> parseForecast(
            String response
    ) {

        try {

            JsonNode daily =
                    objectMapper
                            .readTree(response)
                            .path("daily");

            JsonNode dates =
                    daily.path("time");

            JsonNode maxTemps =
                    daily.path("temperature_2m_max");

            JsonNode minTemps =
                    daily.path("temperature_2m_min");

            JsonNode precipitation =
                    daily.path("precipitation_sum");

            JsonNode precipitationProbability =
                    daily.path(
                            "precipitation_probability_max"
                    );

            JsonNode wind =
                    daily.path(
                            "wind_speed_10m_max"
                    );

            JsonNode weatherCode =
                    daily.path("weather_code");

            List<WeatherData> result =
                    new ArrayList<>();

            for (int i = 0;
                 i < dates.size();
                 i++) {

                int code =
                        weatherCode
                                .get(i)
                                .asInt();

                double probability =
                        precipitationProbability
                                .get(i)
                                .asDouble();

                result.add(
                        WeatherData.builder()
                                .date(
                                        LocalDate.parse(
                                                dates.get(i)
                                                        .asText()
                                        )
                                )
                                .temperatureMax(
                                        maxTemps.get(i)
                                                .asDouble()
                                )
                                .temperatureMin(
                                        minTemps.get(i)
                                                .asDouble()
                                )
                                .precipitation(
                                        precipitation.get(i)
                                                .asDouble()
                                )
                                .precipitationProbability(
                                        probability
                                )
                                .windSpeed(
                                        wind.get(i)
                                                .asDouble()
                                )
                                .weatherCode(code)
                                .condition(
                                        getCondition(code)
                                )
                                .rainExpected(
                                        isRainExpected(
                                                code
                                        )
                                )
                                .build()
                );
            }

            return result;

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to parse Open-Meteo response.",
                    ex
            );
        }
    }

    private String getCondition(
            int weatherCode
    ) {

        return switch (weatherCode) {

            case 0 ->
                    "Clear";

            case 1, 2, 3 ->
                    "Cloudy";

            case 45, 48 ->
                    "Fog";

            case 51, 53, 55,
                 56, 57 ->
                    "Drizzle";

            case 61, 63, 65,
                 66, 67 ->
                    "Rain";

            case 71, 73, 75,
                 77 ->
                    "Snow";

            case 80, 81, 82 ->
                    "Rain Showers";

            case 85, 86 ->
                    "Snow Showers";

            case 95 ->
                    "Thunderstorm";

            case 96, 99 ->
                    "Thunderstorm with Hail";

            default ->
                    "Unknown";
        };
    }

    private boolean isRainExpected(
            int weatherCode
    ) {

        return switch (weatherCode) {

            case 51, 53, 55,
                 56, 57,
                 61, 63, 65,
                 66, 67,
                 80, 81, 82,
                 95, 96, 99 ->
                    true;

            default ->
                    false;
        };
    }
}