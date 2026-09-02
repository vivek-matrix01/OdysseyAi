package com.OdysseyAi.demo.travel.service;

import com.OdysseyAi.demo.travel.dto.DestinationData;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.dto.RouteData;
import com.OdysseyAi.demo.travel.dto.TravelContext;
import com.OdysseyAi.demo.travel.dto.WeatherData;
import com.OdysseyAi.demo.travel.interfaces.CurrencyClient;
import com.OdysseyAi.demo.travel.interfaces.PlacesClient;
import com.OdysseyAi.demo.travel.interfaces.RoutesClient;
import com.OdysseyAi.demo.travel.interfaces.TravelDataService;
import com.OdysseyAi.demo.travel.interfaces.WeatherService;
import com.OdysseyAi.demo.trip.entity.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelDataServiceImpl
        implements TravelDataService {

    private static final String BASE_CURRENCY = "INR";

    private final PlacesClient placesClient;
    private final WeatherService weatherService;
    private final RoutesClient routesClient;
    private final CurrencyClient currencyClient;

    @Override
    public TravelContext buildTravelContext(
            Trip trip,
            String targetCurrency
    ) {

        return buildTravelContext(
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                targetCurrency
        );
    }

    @Override
    public TravelContext buildTravelContext(
            String destinationName,
            LocalDate startDate,
            LocalDate endDate,
            String targetCurrency
    ) {

        validateInput(
                destinationName,
                startDate,
                endDate,
                targetCurrency
        );

        DestinationData destination =
                placesClient.searchDestination(
                        destinationName
                );

        List<PlaceData> attractions =
                placesClient.searchNearby(
                        destination.getLatitude(),
                        destination.getLongitude(),
                        "tourist_attraction",
                        10000
                );

        List<PlaceData> restaurants =
                placesClient.searchNearby(
                        destination.getLatitude(),
                        destination.getLongitude(),
                        "restaurant",
                        10000
                );

        List<WeatherData> weather =
                weatherService.getWeather(
                        destinationName,
                        startDate,
                        endDate
                );

        List<PlaceData> places =
                new ArrayList<>();

        places.addAll(attractions);
        places.addAll(restaurants);

        List<RouteData> routes =
                buildCandidateRoutes(
                        destination,
                        places
                );

        BigDecimal exchangeRate =
                currencyClient.getExchangeRate(
                        BASE_CURRENCY,
                        targetCurrency
                );

        return TravelContext.builder()
                .destination(
                        destination.getName()
                )
                .places(places)
                .weather(weather)
                .routes(routes)
                .baseCurrency(BASE_CURRENCY)
                .targetCurrency(
                        targetCurrency.toUpperCase()
                )
                .exchangeRate(exchangeRate)
                .build();
    }

    @Override
    public TravelContext buildTravelContextWithoutRoutes(
            Trip trip,
            String targetCurrency
    ) {

        return buildTravelContextWithoutRoutes(
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                targetCurrency
        );
    }

    @Override
    public TravelContext buildTravelContextWithoutRoutes(
            String destinationName,
            LocalDate startDate,
            LocalDate endDate,
            String targetCurrency
    ) {

        validateInput(
                destinationName,
                startDate,
                endDate,
                targetCurrency
        );

        DestinationData destination =
                placesClient.searchDestination(
                        destinationName
                );

        List<PlaceData> attractions =
                placesClient.searchNearby(
                        destination.getLatitude(),
                        destination.getLongitude(),
                        "tourist_attraction",
                        10000
                );

        List<PlaceData> restaurants =
                placesClient.searchNearby(
                        destination.getLatitude(),
                        destination.getLongitude(),
                        "restaurant",
                        10000
                );

        List<WeatherData> weather =
                weatherService.getWeather(
                        destinationName,
                        startDate,
                        endDate
                );

        List<PlaceData> places =
                new ArrayList<>();

        places.addAll(attractions);
        places.addAll(restaurants);

        /*
         * No Google Routes calls here.
         *
         * We intentionally keep routes empty because this method
         * is used when generating an itinerary from already
         * selected recommendations.
         */
        List<RouteData> routes =
                new ArrayList<>();

        BigDecimal exchangeRate =
                currencyClient.getExchangeRate(
                        BASE_CURRENCY,
                        targetCurrency
                );

        return TravelContext.builder()
                .destination(
                        destination.getName()
                )
                .places(places)
                .weather(weather)
                .routes(routes)
                .baseCurrency(BASE_CURRENCY)
                .targetCurrency(
                        targetCurrency.toUpperCase()
                )
                .exchangeRate(exchangeRate)
                .build();
    }

    private List<RouteData> buildCandidateRoutes(
            DestinationData destination,
            List<PlaceData> places
    ) {

        List<RouteData> routes =
                new ArrayList<>();

        /*
         * Limit calls during recommendation/generation.
         */
        int limit =
                Math.min(
                        5,
                        places.size()
                );

        for (int i = 0; i < limit; i++) {

            PlaceData place =
                    places.get(i);

            if (place.getLatitude() == null
                    || place.getLongitude() == null) {

                continue;
            }

            RouteData drivingRoute =
                    routesClient.calculateRoute(
                            destination.getName(),
                            destination.getLatitude(),
                            destination.getLongitude(),
                            place.getName(),
                            place.getLatitude(),
                            place.getLongitude(),
                            "DRIVE"
                    );

            routes.add(
                    drivingRoute
            );

            RouteData transitRoute =
                    routesClient.calculateRoute(
                            destination.getName(),
                            destination.getLatitude(),
                            destination.getLongitude(),
                            place.getName(),
                            place.getLatitude(),
                            place.getLongitude(),
                            "TRANSIT"
                    );

            routes.add(
                    transitRoute
            );
        }

        return routes;
    }

    private void validateInput(
            String destination,
            LocalDate startDate,
            LocalDate endDate,
            String targetCurrency
    ) {

        if (destination == null
                || destination.isBlank()) {

            throw new IllegalArgumentException(
                    "Destination is required."
            );
        }

        if (startDate == null) {

            throw new IllegalArgumentException(
                    "Start date is required."
            );
        }

        if (endDate == null) {

            throw new IllegalArgumentException(
                    "End date is required."
            );
        }

        if (endDate.isBefore(startDate)) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date."
            );
        }

        if (targetCurrency == null
                || !targetCurrency.matches(
                "[A-Za-z]{3}"
        )) {

            throw new IllegalArgumentException(
                    "Currency must be a valid 3-letter code."
            );
        }
    }
}