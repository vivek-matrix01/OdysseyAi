package com.OdysseyAi.demo.travel.service;

import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.travel.dto.DestinationData;
import com.OdysseyAi.demo.travel.dto.WeatherData;
import com.OdysseyAi.demo.travel.interfaces.PlacesClient;
import com.OdysseyAi.demo.travel.interfaces.WeatherClient;
import com.OdysseyAi.demo.travel.interfaces.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl
        implements WeatherService {

    private final PlacesClient placesClient;
    private final WeatherClient weatherClient;

    @Override
    public List<WeatherData> getWeather(
            String destination,
            LocalDate startDate,
            LocalDate endDate
    ) {

        validate(
                destination,
                startDate,
                endDate
        );

        DestinationData destinationData =
                placesClient.searchDestination(
                        destination
                );

        return weatherClient.getDailyForecast(
                destinationData.getLatitude(),
                destinationData.getLongitude(),
                startDate,
                endDate
        );
    }

    private void validate(
            String destination,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (destination == null
                || destination.isBlank()) {

            throw new BadRequestException(
                    "Destination is required."
            );
        }

        if (startDate == null) {

            throw new BadRequestException(
                    "Start date is required."
            );
        }

        if (endDate == null) {

            throw new BadRequestException(
                    "End date is required."
            );
        }

        if (endDate.isBefore(startDate)) {

            throw new BadRequestException(
                    "End date cannot be before start date."
            );
        }
    }
}