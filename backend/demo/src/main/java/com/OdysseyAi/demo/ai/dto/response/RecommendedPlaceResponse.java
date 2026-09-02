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
public class RecommendedPlaceResponse {

    private String placeId;

    private String name;

    private String address;

    private String category;

    private Double rating;

    private Integer userRatingCount;

    private String priceLevel;

    private BigDecimal estimatedCost;

    @Builder.Default
    private List<String> imageUrls =
            new ArrayList<>();

    private String reason;
}