package com.OdysseyAi.demo.travel.service;

import com.OdysseyAi.demo.travel.configuration.TransportPricingProperties;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.dto.RouteData;
import com.OdysseyAi.demo.travel.dto.TripCostEstimate;
import com.OdysseyAi.demo.travel.dto.TravelContext;
import com.OdysseyAi.demo.travel.interfaces.CurrencyClient;
import com.OdysseyAi.demo.trip.entity.Activity;
import com.OdysseyAi.demo.trip.entity.Itinerary;
import com.OdysseyAi.demo.trip.entity.ItineraryDay;
import com.OdysseyAi.demo.trip.entity.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TripCostEstimator {

    private final CurrencyClient currencyClient;
    private final TransportPricingProperties transportPricingProperties;

    public TripCostEstimate estimate(
            Trip trip,
            Itinerary itinerary,
            TravelContext travelContext,
            int numberOfTravelers,
            BigDecimal nightlyHotelCost
    ) {

        if (numberOfTravelers < 1) {
            throw new IllegalArgumentException(
                    "Number of travelers must be at least 1."
            );
        }

        String targetCurrency =
                travelContext.getTargetCurrency();

        long hotelNights =
                ChronoUnit.DAYS.between(
                        trip.getStartDate(),
                        trip.getEndDate()
                );

        int tripDays =
                Math.toIntExact(
                        hotelNights + 1
                );

        BigDecimal safeHotelCost =
                safe(nightlyHotelCost);

        BigDecimal accommodationCost =
                safeHotelCost.multiply(
                        BigDecimal.valueOf(hotelNights)
                );

        BigDecimal dailyMealCostPerTraveler =
                estimateDailyMealCostPerTraveler(
                        travelContext,
                        targetCurrency
                );

        BigDecimal foodCost =
                dailyMealCostPerTraveler
                        .multiply(
                                BigDecimal.valueOf(
                                        numberOfTravelers
                                )
                        )
                        .multiply(
                                BigDecimal.valueOf(
                                        tripDays
                                )
                        );

        BigDecimal activityCost =
                calculateActivityCost(
                        itinerary,
                        targetCurrency
                );

        BigDecimal transportDistanceKm =
                calculateTransportDistanceKm(
                        travelContext
                );

        BigDecimal transportCost =
                calculateTransportCost(
                        travelContext,
                        targetCurrency
                );

        BigDecimal totalCost =
                accommodationCost
                        .add(foodCost)
                        .add(activityCost)
                        .add(transportCost);

        BigDecimal perTravelerCost =
                totalCost.divide(
                        BigDecimal.valueOf(
                                numberOfTravelers
                        ),
                        2,
                        RoundingMode.HALF_UP
                );

        return TripCostEstimate.builder()
                .currency(targetCurrency)
                .numberOfTravelers(numberOfTravelers)
                .tripDays(tripDays)
                .hotelNights(
                        Math.toIntExact(
                                hotelNights
                        )
                )
                .nightlyHotelCost(
                        money(safeHotelCost)
                )
                .dailyMealCostPerTraveler(
                        money(
                                dailyMealCostPerTraveler
                        )
                )
                .estimatedTransportDistanceKm(
                        money(
                                transportDistanceKm
                        )
                )
                .estimatedTransportCost(
                        money(
                                transportCost
                        )
                )
                .accommodationCost(
                        money(
                                accommodationCost
                        )
                )
                .foodCost(
                        money(foodCost)
                )
                .transportCost(
                        money(transportCost)
                )
                .activityCost(
                        money(activityCost)
                )
                .otherCost(
                        BigDecimal.ZERO
                )
                .totalCost(
                        money(totalCost)
                )
                .perTravelerCost(
                        money(perTravelerCost)
                )
                .build();
    }

    private BigDecimal estimateDailyMealCostPerTraveler(
            TravelContext travelContext,
            String targetCurrency
    ) {

        if (travelContext.getPlaces() == null
                || travelContext.getPlaces().isEmpty()) {

            return BigDecimal.ZERO;
        }

        BigDecimal total =
                BigDecimal.ZERO;

        int restaurantCount = 0;

        for (PlaceData place :
                travelContext.getPlaces()) {

            String type =
                    place.getPrimaryType();

            if (type == null) {
                continue;
            }

            if (!type.toLowerCase()
                    .contains("restaurant")) {
                continue;
            }

            BigDecimal restaurantCost =
                    estimateRestaurantMealCost(
                            place
                    );

            if (restaurantCost.signum() <= 0) {
                continue;
            }

            total =
                    total.add(
                            restaurantCost
                    );

            restaurantCount++;
        }

        if (restaurantCount == 0) {
            return BigDecimal.ZERO;
        }

        /*
         * The Google Places price level is converted into
         * our own application-level meal estimate.
         */
        BigDecimal averageMealCost =
                total.divide(
                        BigDecimal.valueOf(
                                restaurantCount
                        ),
                        2,
                        RoundingMode.HALF_UP
                );

        /*
         * Three meals per traveler per day.
         */
        BigDecimal dailyCost =
                averageMealCost.multiply(
                        BigDecimal.valueOf(3)
                );

        return dailyCost;
    }

    private BigDecimal estimateRestaurantMealCost(
            PlaceData place
    ) {

        String priceLevel =
                place.getPriceLevel();

        if (priceLevel == null) {
            return BigDecimal.ZERO;
        }

        return switch (priceLevel) {

            case "PRICE_LEVEL_FREE" ->
                    BigDecimal.ZERO;

            case "PRICE_LEVEL_INEXPENSIVE" ->
                    BigDecimal.valueOf(200);

            case "PRICE_LEVEL_MODERATE" ->
                    BigDecimal.valueOf(500);

            case "PRICE_LEVEL_EXPENSIVE" ->
                    BigDecimal.valueOf(1000);

            case "PRICE_LEVEL_VERY_EXPENSIVE" ->
                    BigDecimal.valueOf(2000);

            default ->
                    BigDecimal.ZERO;
        };
    }

    private BigDecimal calculateActivityCost(
            Itinerary itinerary,
            String targetCurrency
    ) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (ItineraryDay day :
                itinerary.getDays()) {

            for (Activity activity :
                    day.getActivities()) {

                BigDecimal activityCost =
                        activity.getEstimatedCost();

                if (activityCost == null) {
                    continue;
                }

                String sourceCurrency =
                        activity.getCurrency();

                if (sourceCurrency == null
                        || sourceCurrency.isBlank()) {
                    continue;
                }

                BigDecimal convertedCost =
                        convertCurrency(
                                activityCost,
                                sourceCurrency,
                                targetCurrency
                        );

                total =
                        total.add(
                                convertedCost
                        );
            }
        }

        return total;
    }

    private BigDecimal calculateTransportDistanceKm(
            TravelContext travelContext
    ) {

        if (travelContext.getRoutes() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalMeters =
                BigDecimal.ZERO;

        for (RouteData route :
                travelContext.getRoutes()) {

            if (route.getDistanceMeters() == null) {
                continue;
            }

            /*
             * Only count DRIVE routes here.
             *
             * We do not want the same physical distance
             * counted twice because both DRIVE and TRANSIT
             * routes are stored.
             */
            if (!"DRIVE".equalsIgnoreCase(
                    route.getTravelMode()
            )) {
                continue;
            }

            totalMeters =
                    totalMeters.add(
                            BigDecimal.valueOf(
                                    route.getDistanceMeters()
                            )
                    );
        }

        return totalMeters.divide(
                BigDecimal.valueOf(1000),
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal calculateTransportCost(
            TravelContext travelContext,
            String targetCurrency
    ) {

        if (travelContext.getRoutes() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total =
                BigDecimal.ZERO;

        for (RouteData route :
                travelContext.getRoutes()) {

            if (route.getTravelMode() == null) {
                continue;
            }

            if ("TRANSIT".equalsIgnoreCase(
                    route.getTravelMode()
            )) {

                /*
                 * Use Google's transit fare when available.
                 */
                if (route.getFare() != null
                        && route.getFareCurrency() != null) {

                    BigDecimal convertedFare =
                            convertCurrency(
                                    route.getFare(),
                                    route.getFareCurrency(),
                                    targetCurrency
                            );

                    total =
                            total.add(
                                    convertedFare
                            );
                }

            } else if ("DRIVE".equalsIgnoreCase(
                    route.getTravelMode()
            )) {

                /*
                 * Google gives distance.
                 *
                 * Our application applies the configured
                 * driving cost per kilometre.
                 */
                if (route.getDistanceMeters() == null) {
                    continue;
                }

                BigDecimal distanceKm =
                        BigDecimal.valueOf(
                                route.getDistanceMeters()
                        ).divide(
                                BigDecimal.valueOf(1000),
                                2,
                                RoundingMode.HALF_UP
                        );

                BigDecimal drivingCost =
                        distanceKm.multiply(
                                transportPricingProperties
                                        .getDrivingCostPerKm()
                        );

                total =
                        total.add(
                                drivingCost
                        );
            }
        }

        /*
         * Our route context currently contains routes from the
         * destination to candidate places. Therefore this is a
         * candidate-route estimate, not a complete door-to-door
         * transport bill.
         */
        return total;
    }

    private BigDecimal convertCurrency(
            BigDecimal amount,
            String sourceCurrency,
            String targetCurrency
    ) {

        if (sourceCurrency.equalsIgnoreCase(
                targetCurrency
        )) {
            return amount;
        }

        BigDecimal exchangeRate =
                currencyClient.getExchangeRate(
                        sourceCurrency,
                        targetCurrency
                );

        return amount.multiply(
                exchangeRate
        );
    }

    private BigDecimal safe(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private BigDecimal money(
            BigDecimal value
    ) {

        return safe(value)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }
}