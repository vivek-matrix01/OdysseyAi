package com.OdysseyAi.demo.ai.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedHotelResponse {

    private String propertyToken;

    private String name;

    private String address;

    private Double rating;

    private Integer reviewCount;

    private Integer hotelClass;

    private BigDecimal estimatedNightlyCost;

    private BigDecimal totalCost;

    private String currency;

    private String priceDisplay;

    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    private String reason;
}