package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.response.RecommendedPlaceResponse;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.interfaces.PlacePhotoClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlaceRecommendationEnricher {

    private final PlacePhotoClient placePhotoClient;

    public PlaceRecommendationEnricher(
            PlacePhotoClient placePhotoClient
    ) {
        this.placePhotoClient = placePhotoClient;
    }

    public TripRecommendationResponse enrich(
            TripRecommendationResponse recommendation,
            List<PlaceData> places
    ) {

        if (recommendation == null
                || recommendation.getPlaces() == null
                || recommendation.getPlaces().isEmpty()
                || places == null
                || places.isEmpty()) {

            return recommendation;
        }

        List<RecommendedPlaceResponse> enrichedPlaces =
                new ArrayList<>();

        for (RecommendedPlaceResponse recommendedPlace :
                recommendation.getPlaces()) {

            PlaceData matchingPlace =
                    findMatchingPlace(
                            recommendedPlace.getPlaceId(),
                            places
                    );

            if (matchingPlace == null) {
                continue;
            }

            List<String> imageUrls =
                    new ArrayList<>();

            if (matchingPlace.getPhotoNames() != null) {

                int limit =
                        Math.min(
                                3,
                                matchingPlace.getPhotoNames().size()
                        );

                for (int i = 0; i < limit; i++) {

                    String photoName =
                            matchingPlace
                                    .getPhotoNames()
                                    .get(i);

                    try {

                        String photoUrl =
                                placePhotoClient.getPhotoUrl(
                                        photoName,
                                        800,
                                        600
                                );

                        if (photoUrl != null
                                && !photoUrl.isBlank()) {

                            imageUrls.add(photoUrl);
                        }

                    } catch (Exception ignored) {
                        /*
                         * Photo failure should not remove
                         * the place recommendation.
                         */
                    }
                }
            }

            RecommendedPlaceResponse enrichedPlace =
                    RecommendedPlaceResponse.builder()
                            .placeId(
                                    matchingPlace.getId()
                            )
                            .name(
                                    matchingPlace.getName()
                            )
                            .address(
                                    matchingPlace.getAddress()
                            )
                            .category(
                                    matchingPlace.getPrimaryType()
                            )
                            .rating(
                                    matchingPlace.getRating()
                            )
                            .userRatingCount(
                                    matchingPlace
                                            .getUserRatingCount()
                            )
                            .priceLevel(
                                    matchingPlace.getPriceLevel()
                            )
                            .estimatedCost(
                                    recommendedPlace
                                            .getEstimatedCost()
                            )
                            .imageUrls(
                                    imageUrls
                            )
                            .reason(
                                    recommendedPlace.getReason()
                            )
                            .build();

            enrichedPlaces.add(
                    enrichedPlace
            );
        }

        recommendation.setPlaces(
                enrichedPlaces
        );

        return recommendation;
    }

    private PlaceData findMatchingPlace(
            String placeId,
            List<PlaceData> places
    ) {

        if (placeId == null
                || placeId.isBlank()) {

            return null;
        }

        for (PlaceData place :
                places) {

            if (place.getId() == null) {
                continue;
            }

            if (place.getId().equals(placeId)) {
                return place;
            }
        }

        return null;
    }
}