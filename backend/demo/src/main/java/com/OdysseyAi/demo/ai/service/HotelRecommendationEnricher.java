package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.response.RecommendedHotelResponse;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;
import com.OdysseyAi.demo.travel.dto.HotelData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HotelRecommendationEnricher {

    public TripRecommendationResponse enrich(
            TripRecommendationResponse recommendation,
            List<HotelData> hotelData
    ) {

        if (recommendation == null
                || recommendation.getHotels() == null
                || hotelData == null
                || hotelData.isEmpty()) {

            return recommendation;
        }

        List<RecommendedHotelResponse> enrichedHotels =
                new ArrayList<>();

        for (RecommendedHotelResponse recommendedHotel :
                recommendation.getHotels()) {

            HotelData matchingHotel =
                    findMatchingHotel(
                            recommendedHotel.getPropertyToken(),
                            hotelData
                    );

            if (matchingHotel == null) {
                continue;
            }

            RecommendedHotelResponse enrichedHotel =
                    RecommendedHotelResponse.builder()
                            .propertyToken(
                                    matchingHotel.getPropertyToken()
                            )
                            .name(
                                    matchingHotel.getName()
                            )
                            .address(
                                    matchingHotel.getAddress()
                            )
                            .rating(
                                    matchingHotel.getRating()
                            )
                            .reviewCount(
                                    matchingHotel.getReviewCount()
                            )
                            .hotelClass(
                                    matchingHotel.getHotelClass()
                            )
                            .estimatedNightlyCost(
                                    matchingHotel.getNightlyPrice()
                            )
                            .totalCost(
                                    matchingHotel.getTotalPrice()
                            )
                            .currency(
                                    recommendation.getCurrency()
                            )
                            .priceDisplay(
                                    matchingHotel.getPriceDisplay()
                            )
                            .amenities(
                                    matchingHotel.getAmenities()
                            )
                            .imageUrls(
                                    matchingHotel.getImageUrls()
                            )
                            .reason(
                                    recommendedHotel.getReason()
                            )
                            .build();

            enrichedHotels.add(
                    enrichedHotel
            );
        }

        recommendation.setHotels(
                enrichedHotels
        );

        return recommendation;
    }

    private HotelData findMatchingHotel(
            String propertyToken,
            List<HotelData> hotels
    ) {

        if (propertyToken == null
                || propertyToken.isBlank()) {

            return null;
        }

        for (HotelData hotel :
                hotels) {

            if (hotel.getPropertyToken() == null) {
                continue;
            }

            if (hotel.getPropertyToken()
                    .equals(propertyToken)) {

                return hotel;
            }
        }

        return null;
    }
    private RecommendedHotelResponse toRecommendedHotel(
            HotelData hotel
    ) {

        return RecommendedHotelResponse.builder()
                .propertyToken(
                        hotel.getPropertyToken()
                )
                .name(
                        hotel.getName()
                )
                .address(
                        hotel.getAddress()
                )
                .rating(
                        hotel.getRating()
                )
                .reviewCount(
                        hotel.getReviewCount()
                )
                .hotelClass(
                        hotel.getHotelClass()
                )
                .estimatedNightlyCost(
                        hotel.getNightlyPrice()
                )
                .totalCost(
                        hotel.getTotalPrice()
                )
                .currency(
                        hotel.getCurrency()
                )
                .priceDisplay(
                        hotel.getPriceDisplay()
                )
                .amenities(
                        hotel.getAmenities() == null
                                ? new ArrayList<>()
                                : new ArrayList<>(
                                hotel.getAmenities()
                        )
                )
                .imageUrls(
                        hotel.getImageUrls() == null
                                ? new ArrayList<>()
                                : new ArrayList<>(
                                hotel.getImageUrls()
                        )
                )
                .build();
    }
}