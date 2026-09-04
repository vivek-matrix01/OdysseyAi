package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.request.TripRecommendationRequest;
import com.OdysseyAi.demo.ai.dto.response.RecommendedPlaceResponse;
import com.OdysseyAi.demo.ai.dto.response.RouteSegmentResponse;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;
import com.OdysseyAi.demo.common.enums.TransportMode;
import com.OdysseyAi.demo.travel.configuration.TransportPricingProperties;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.dto.RouteData;
import com.OdysseyAi.demo.travel.interfaces.RoutesClient;
import com.OdysseyAi.demo.travel.service.DestinationCurrencyService;
import com.OdysseyAi.demo.travel.service.DestinationCurrencyService.DestinationCurrencyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendationTransportEstimator {

    private final RoutesClient routesClient;
    private final TransportPricingProperties transportPricingProperties;
    private final DestinationCurrencyService destinationCurrencyService;

    // Benchmark fuel costs per KM for India fallback
    private static final BigDecimal DEFAULT_PETROL_COST_PER_KM = BigDecimal.valueOf(14.00);
    private static final BigDecimal DEFAULT_DIESEL_COST_PER_KM = BigDecimal.valueOf(11.50);

    public BigDecimal estimate(
            TripRecommendationRequest request,
            TripRecommendationResponse recommendation,
            List<PlaceData> availablePlaces
    ) {

        DestinationCurrencyInfo destInfo = destinationCurrencyService.resolveDestination(
                request != null ? request.getDestination() : (recommendation != null ? recommendation.getDestination() : "")
        );

        if (recommendation != null && destInfo != null) {
            recommendation.setLocalCurrency(destInfo.getCurrencyCode());
            recommendation.setLocalCurrencySymbol(destInfo.getCurrencySymbol());
            recommendation.setExchangeRateFromInr(destInfo.getExchangeRateFromInr());
            recommendation.setLocalPetrolPricePerLiter(destInfo.getPetrolPricePerLiterLocal());
        }

        if (recommendation == null
                || recommendation.getPlaces() == null
                || recommendation.getPlaces().size() < 2
                || availablePlaces == null
                || availablePlaces.isEmpty()) {

            return BigDecimal.ZERO;
        }

        Map<String, PlaceData> placesById = new HashMap<>();

        for (PlaceData place : availablePlaces) {
            if (place.getId() != null && !place.getId().isBlank()) {
                placesById.put(place.getId(), place);
            }
        }

        BigDecimal totalFare = BigDecimal.ZERO;
        double totalDistanceKm = 0.0;
        List<RouteSegmentResponse> segments = new ArrayList<>();

        TransportMode transportMode = TransportMode.DRIVE;

        BigDecimal baseRatePerKm = DEFAULT_PETROL_COST_PER_KM;
        if (destInfo != null && destInfo.getDrivingCostPerKmLocal() != null) {
            if ("INR".equalsIgnoreCase(destInfo.getCurrencyCode())) {
                baseRatePerKm = destInfo.getDrivingCostPerKmLocal();
            } else if (destInfo.getExchangeRateToInr() != null && destInfo.getExchangeRateToInr().compareTo(BigDecimal.ZERO) > 0) {
                baseRatePerKm = destInfo.getDrivingCostPerKmLocal().multiply(destInfo.getExchangeRateToInr()).setScale(2, RoundingMode.HALF_UP);
            }
        } else if (transportPricingProperties != null && transportPricingProperties.getDrivingCostPerKm() != null) {
            baseRatePerKm = transportPricingProperties.getDrivingCostPerKm();
        }

        for (int i = 0; i < recommendation.getPlaces().size() - 1; i++) {

            RecommendedPlaceResponse originRecommendation = recommendation.getPlaces().get(i);
            RecommendedPlaceResponse destinationRecommendation = recommendation.getPlaces().get(i + 1);

            PlaceData origin = placesById.get(originRecommendation.getPlaceId());
            PlaceData destination = placesById.get(destinationRecommendation.getPlaceId());

            if (origin == null || destination == null) {
                continue;
            }

            double distanceKm = 0.0;
            long durationMinutes = 15;

            // 1. Try Google Routes API first
            if (origin.getLatitude() != null && origin.getLongitude() != null
                    && destination.getLatitude() != null && destination.getLongitude() != null) {

                try {
                    RouteData route = routesClient.calculateRoute(
                            origin.getName(),
                            origin.getLatitude(),
                            origin.getLongitude(),
                            destination.getName(),
                            destination.getLatitude(),
                            destination.getLongitude(),
                            transportMode.name()
                    );

                    if (route != null && route.getDistanceMeters() != null && route.getDistanceMeters() > 0) {
                        distanceKm = route.getDistanceMeters() / 1000.0;
                        if (route.getDurationSeconds() != null && route.getDurationSeconds() > 0) {
                            durationMinutes = Math.max(5, route.getDurationSeconds() / 60);
                        }
                    }
                } catch (Exception ignored) {}

                // 2. Fallback to Haversine * 1.35 if Google distance is unavailable
                if (distanceKm <= 0.0) {
                    double rawKm = calculateHaversineKm(
                            origin.getLatitude(),
                            origin.getLongitude(),
                            destination.getLatitude(),
                            destination.getLongitude()
                    ) * 1.35;
                    distanceKm = Math.max(1.5, Math.round(rawKm * 10.0) / 10.0);
                    durationMinutes = Math.max(8, Math.round(distanceKm * 2.2));
                }
            } else {
                distanceKm = 3.5;
                durationMinutes = 15;
            }

            BigDecimal segmentFare = baseRatePerKm.multiply(BigDecimal.valueOf(distanceKm)).setScale(2, RoundingMode.HALF_UP);
            totalFare = totalFare.add(segmentFare);
            totalDistanceKm += distanceKm;

            segments.add(RouteSegmentResponse.builder()
                    .originName(origin.getName())
                    .destinationName(destination.getName())
                    .distanceKm(Math.round(distanceKm * 10.0) / 10.0)
                    .durationMinutes(durationMinutes)
                    .estimatedCost(segmentFare)
                    .build());
        }

        double roundedTotalDistance = Math.round(totalDistanceKm * 10.0) / 10.0;
        BigDecimal petrolCost = baseRatePerKm.multiply(BigDecimal.valueOf(roundedTotalDistance)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dieselCost = baseRatePerKm.multiply(BigDecimal.valueOf(0.85)).multiply(BigDecimal.valueOf(roundedTotalDistance)).setScale(2, RoundingMode.HALF_UP);

        recommendation.setTotalDistanceKm(roundedTotalDistance);
        recommendation.setPetrolVehicleCost(petrolCost);
        recommendation.setDieselVehicleCost(dieselCost);
        recommendation.setRouteSegments(segments);

        return totalFare.setScale(2, RoundingMode.HALF_UP);
    }

    private double calculateHaversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}