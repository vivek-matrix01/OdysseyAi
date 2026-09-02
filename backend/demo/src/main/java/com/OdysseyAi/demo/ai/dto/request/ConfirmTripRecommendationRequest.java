package com.OdysseyAi.demo.ai.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConfirmTripRecommendationRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 100)
    private String destination;

    @NotNull
    @FutureOrPresent
    private LocalDate startDate;

    @NotNull
    @FutureOrPresent
    private LocalDate endDate;

    /*
     * These are NOT stored in Trip yet.
     * They will be used when generating the itinerary.
     */
    private String selectedHotelPropertyToken;

    private List<String> selectedPlaceIds =
            new ArrayList<>();
}