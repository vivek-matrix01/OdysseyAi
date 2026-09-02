package com.OdysseyAi.demo.travel.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceData {

    private String id;

    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    private String primaryType;

    private Double rating;

    private Integer userRatingCount;

    private String priceLevel;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    @Builder.Default
    private List<String> photoNames = new ArrayList<>();
}