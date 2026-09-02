package com.OdysseyAi.demo.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateSelectedItineraryRequest {

    @NotNull
    private Long tripId;

    private SelectedHotelRequest hotel;

    @NotEmpty
    @Builder.Default
    private List<String> selectedPlaceIds =
            new ArrayList<>();

    @NotBlank
    private String currency;

    private String additionalPreferences;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelectedHotelRequest {

        private String propertyToken;

        private String name;

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
        private List<String> amenities =
                new ArrayList<>();

        @Builder.Default
        private List<String> imageUrls =
                new ArrayList<>();
    }
}