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
public class HotelData {

    private String propertyToken;

    private String name;

    private String description;

    private String address;

    private Double latitude;

    private Double longitude;

    private Double rating;

    private Integer reviewCount;

    private Integer hotelClass;

    private BigDecimal nightlyPrice;

    private BigDecimal totalPrice;

    private BigDecimal beforeTaxesNightlyPrice;

    private String currency;

    private String priceDisplay;

    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
}