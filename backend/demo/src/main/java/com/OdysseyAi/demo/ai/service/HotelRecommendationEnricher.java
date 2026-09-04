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

            java.math.BigDecimal nightly = resolveNightlyPrice(matchingHotel);
            java.math.BigDecimal total = resolveTotalPrice(matchingHotel, nightly);

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
                                    nightly
                            )
                            .totalCost(
                                    total
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
                                    resolveHotelImages(matchingHotel)
                            )
                            .reason(
                                    recommendedHotel.getReason()
                            )
                            .build();

            enrichedHotels.add(
                    enrichedHotel
            );
        }

        java.util.Set<String> addedTokens = new java.util.HashSet<>();
        for (RecommendedHotelResponse h : enrichedHotels) {
            if (h.getPropertyToken() != null) {
                addedTokens.add(h.getPropertyToken());
            }
        }

        for (HotelData hotel : hotelData) {
            if (hotel.getPropertyToken() == null || addedTokens.contains(hotel.getPropertyToken())) {
                continue;
            }
            if (enrichedHotels.size() >= 40) {
                break;
            }

            java.math.BigDecimal nightly = resolveNightlyPrice(hotel);
            java.math.BigDecimal total = resolveTotalPrice(hotel, nightly);

            enrichedHotels.add(
                    RecommendedHotelResponse.builder()
                            .propertyToken(hotel.getPropertyToken())
                            .name(hotel.getName())
                            .address(hotel.getAddress())
                            .rating(hotel.getRating())
                            .reviewCount(hotel.getReviewCount())
                            .hotelClass(hotel.getHotelClass())
                            .estimatedNightlyCost(nightly)
                            .totalCost(total)
                            .currency(recommendation.getCurrency())
                            .priceDisplay(hotel.getPriceDisplay())
                            .amenities(hotel.getAmenities())
                            .imageUrls(resolveHotelImages(hotel))
                            .reason("Great accommodation option in " + (recommendation.getDestination() != null ? recommendation.getDestination() : "the area"))
                            .build()
            );
            addedTokens.add(hotel.getPropertyToken());
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

    private java.math.BigDecimal resolveNightlyPrice(HotelData hotel) {
        if (hotel == null) return java.math.BigDecimal.valueOf(2500);
        if (hotel.getNightlyPrice() != null && hotel.getNightlyPrice().signum() > 0) {
            return hotel.getNightlyPrice();
        }
        if (hotel.getPriceDisplay() != null) {
            String clean = hotel.getPriceDisplay().replaceAll("[^0-9.]", "");
            if (!clean.isBlank()) {
                try {
                    return new java.math.BigDecimal(clean);
                } catch (Exception ignored) {}
            }
        }
        return java.math.BigDecimal.valueOf(2500);
    }

    private java.math.BigDecimal resolveTotalPrice(HotelData hotel, java.math.BigDecimal nightly) {
        if (hotel != null && hotel.getTotalPrice() != null && hotel.getTotalPrice().signum() > 0) {
            return hotel.getTotalPrice();
        }
        if (nightly != null) {
            return nightly.multiply(java.math.BigDecimal.valueOf(3));
        }
        return java.math.BigDecimal.valueOf(7500);
    }

    private List<String> resolveHotelImages(HotelData hotel) {
        if (hotel != null && hotel.getImageUrls() != null && !hotel.getImageUrls().isEmpty()) {
            return hotel.getImageUrls();
        }
        return getBackupHotelImages(hotel);
    }

    private List<String> getBackupHotelImages(HotelData hotel) {
        String name = hotel != null && hotel.getName() != null ? hotel.getName().toLowerCase() : "";
        int hash = Math.abs(name.hashCode());

        String[] resortImages = {
                "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80"
        };

        String[] hotelRoomImages = {
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80"
        };

        String selected;
        if (name.contains("resort") || name.contains("beach") || name.contains("spa") || name.contains("villa")) {
            selected = resortImages[hash % resortImages.length];
        } else {
            selected = hotelRoomImages[hash % hotelRoomImages.length];
        }

        return List.of(selected);
    }
}