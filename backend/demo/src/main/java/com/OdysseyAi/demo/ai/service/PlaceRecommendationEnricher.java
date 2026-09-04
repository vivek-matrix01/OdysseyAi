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

        int serpApiPhotoCount = 0;
        final int MAX_SERPAPI_PHOTO_LOOKUPS = 39;

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

            if (serpApiPhotoCount < MAX_SERPAPI_PHOTO_LOOKUPS) {
                try {
                    String query = matchingPlace.getName() + " " + matchingPlace.getAddress();
                    List<String> fetchedUrls = placePhotoClient.getPhotoUrls(query);
                    if (fetchedUrls != null && !fetchedUrls.isEmpty()) {
                        imageUrls.addAll(fetchedUrls);
                        serpApiPhotoCount++;
                    }
                } catch (Exception ignored) {
                    /*
                     * Photo failure should not remove
                     * the place recommendation.
                     */
                }
            }

            if (imageUrls.isEmpty()) {
                imageUrls = getBackupPlaceImages(matchingPlace);
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
                            .latitude(
                                    matchingPlace.getLatitude()
                            )
                            .longitude(
                                    matchingPlace.getLongitude()
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
                                    recommendedPlace.getEstimatedCost() != null && recommendedPlace.getEstimatedCost().signum() > 0
                                            ? recommendedPlace.getEstimatedCost()
                                            : getEstimatedPlaceCost(matchingPlace)
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

        java.util.Set<String> addedPlaceIds = new java.util.HashSet<>();
        for (RecommendedPlaceResponse p : enrichedPlaces) {
            if (p.getPlaceId() != null) {
                addedPlaceIds.add(p.getPlaceId());
            }
        }

        for (PlaceData place : places) {
            if (place == null || place.getId() == null || addedPlaceIds.contains(place.getId())) {
                continue;
            }
            if (enrichedPlaces.size() >= 50) {
                break;
            }

            List<String> imageUrls = new ArrayList<>();
            // Only fetch photo if quota allows, otherwise fallback to curated backup image
            if (serpApiPhotoCount < MAX_SERPAPI_PHOTO_LOOKUPS) {
                try {
                    String query = place.getName() + " " + place.getAddress();
                    List<String> fetchedUrls = placePhotoClient.getPhotoUrls(query);
                    if (fetchedUrls != null && !fetchedUrls.isEmpty()) {
                        imageUrls.addAll(fetchedUrls);
                        serpApiPhotoCount++;
                    }
                } catch (Exception ignored) {}
            }

            if (imageUrls.isEmpty()) {
                imageUrls = getBackupPlaceImages(place);
            }

            enrichedPlaces.add(
                    RecommendedPlaceResponse.builder()
                            .placeId(place.getId())
                            .name(place.getName())
                            .address(place.getAddress())
                            .category(place.getPrimaryType() != null ? place.getPrimaryType() : "Attraction")
                            .latitude(place.getLatitude())
                            .longitude(place.getLongitude())
                            .rating(place.getRating())
                            .userRatingCount(place.getUserRatingCount())
                            .priceLevel(place.getPriceLevel())
                            .estimatedCost(
                                    place.getMinPrice() != null && place.getMinPrice().signum() > 0
                                            ? place.getMinPrice()
                                            : getEstimatedPlaceCost(place)
                            )
                            .imageUrls(imageUrls)
                            .reason("Popular attraction in " + (place.getAddress() != null ? place.getAddress() : (recommendation.getDestination() != null ? recommendation.getDestination() : "the area")))
                            .build()
            );
            addedPlaceIds.add(place.getId());
        }

        recommendation.setPlaces(
                enrichedPlaces
        );

        return recommendation;
    }

    private java.math.BigDecimal getEstimatedPlaceCost(PlaceData place) {
        if (place == null) return java.math.BigDecimal.valueOf(150);
        String category = place.getPrimaryType() != null ? place.getPrimaryType().toLowerCase() : "";
        if (category.contains("restaurant") || category.contains("food") || category.contains("cafe") || category.contains("bar")) {
            return java.math.BigDecimal.valueOf(450);
        } else if (category.contains("museum") || category.contains("art") || category.contains("gallery")) {
            return java.math.BigDecimal.valueOf(250);
        } else if (category.contains("park") || category.contains("garden") || category.contains("zoo")) {
            return java.math.BigDecimal.valueOf(150);
        } else if (category.contains("amusement") || category.contains("water_park") || category.contains("tourist")) {
            return java.math.BigDecimal.valueOf(350);
        } else if (category.contains("hindu") || category.contains("church") || category.contains("temple") || category.contains("mosque") || category.contains("place_of_worship")) {
            return java.math.BigDecimal.valueOf(50);
        } else {
            return java.math.BigDecimal.valueOf(200);
        }
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

    private List<String> getBackupPlaceImages(PlaceData place) {
        String name = place != null && place.getName() != null ? place.getName().toLowerCase() : "";
        String type = place != null && place.getPrimaryType() != null ? place.getPrimaryType().toLowerCase() : "";
        String combined = name + " " + type;

        int hash = Math.abs(name.hashCode());

        String[] beachImages = {
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1519046904884-53103b34b206?auto=format&fit=crop&w=800&q=80"
        };

        String[] templeImages = {
                "https://images.unsplash.com/photo-1590050752117-238cb0fb12b1?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1564507592333-c60657eea523?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=800&q=80"
        };

        String[] natureImages = {
                "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80"
        };

        String[] foodImages = {
                "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=800&q=80"
        };

        String[] generalImages = {
                "https://images.unsplash.com/photo-1517869662768-5c25ce264969?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=800&q=80"
        };

        String selected;
        if (combined.contains("beach") || combined.contains("coast") || combined.contains("sea") || combined.contains("cove") || combined.contains("island")) {
            selected = beachImages[hash % beachImages.length];
        } else if (combined.contains("temple") || combined.contains("church") || combined.contains("fort") || combined.contains("palace") || combined.contains("monument") || combined.contains("historical") || combined.contains("mosque")) {
            selected = templeImages[hash % templeImages.length];
        } else if (combined.contains("falls") || combined.contains("waterfall") || combined.contains("wildlife") || combined.contains("sanctuary") || combined.contains("lake") || combined.contains("park") || combined.contains("forest") || combined.contains("garden")) {
            selected = natureImages[hash % natureImages.length];
        } else if (combined.contains("restaurant") || combined.contains("cafe") || combined.contains("bar") || combined.contains("food") || combined.contains("dining") || combined.contains("bistro")) {
            selected = foodImages[hash % foodImages.length];
        } else {
            selected = generalImages[hash % generalImages.length];
        }

        return List.of(selected);
    }
}