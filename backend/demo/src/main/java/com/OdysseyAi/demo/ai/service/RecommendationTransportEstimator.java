package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.request.TripRecommendationRequest;
import com.OdysseyAi.demo.ai.dto.response.RecommendedPlaceResponse;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;
import com.OdysseyAi.demo.common.enums.TransportMode;
import com.OdysseyAi.demo.travel.configuration.TransportPricingProperties;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.dto.RouteData;
import com.OdysseyAi.demo.travel.interfaces.RoutesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendationTransportEstimator {

    private final RoutesClient routesClient;
    private final TransportPricingProperties transportPricingProperties;

    public BigDecimal estimate(
            TripRecommendationRequest request,
            TripRecommendationResponse recommendation,
            List<PlaceData> availablePlaces
    ) {

        if (recommendation == null
                || recommendation.getPlaces() == null
                || recommendation.getPlaces().size() < 2
                || availablePlaces == null
                || availablePlaces.isEmpty()) {

            return BigDecimal.ZERO;
        }

        Map<String, PlaceData> placesById =
                new HashMap<>();

        for (PlaceData place : availablePlaces) {

            if (place.getId() != null
                    && !place.getId().isBlank()) {

                placesById.put(
                        place.getId(),
                        place
                );
            }
        }

        BigDecimal total =
                BigDecimal.ZERO;

        TransportMode transportMode =
                TransportMode.TRANSIT;

        for (int i = 0;
             i < recommendation.getPlaces().size() - 1;
             i++) {

            RecommendedPlaceResponse originRecommendation =
                    recommendation.getPlaces().get(i);

            RecommendedPlaceResponse destinationRecommendation =
                    recommendation.getPlaces().get(i + 1);

            PlaceData origin =
                    placesById.get(
                            originRecommendation.getPlaceId()
                    );

            PlaceData destination =
                    placesById.get(
                            destinationRecommendation.getPlaceId()
                    );

            if (origin == null
                    || destination == null
                    || origin.getLatitude() == null
                    || origin.getLongitude() == null
                    || destination.getLatitude() == null
                    || destination.getLongitude() == null) {

                continue;
            }

            RouteData route =
                    routesClient.calculateRoute(
                            origin.getName(),
                            origin.getLatitude(),
                            origin.getLongitude(),
                            destination.getName(),
                            destination.getLatitude(),
                            destination.getLongitude(),
                            transportMode.name()
                    );

            if (route.getFare() == null
                    || route.getFareCurrency() == null) {

                continue;
            }

            BigDecimal fare =
                    route.getFare()
                            .multiply(
                                    BigDecimal.valueOf(
                                            request.getTravelers()
                                    )
                            );

            total =
                    total.add(fare);
        }

        return total.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}