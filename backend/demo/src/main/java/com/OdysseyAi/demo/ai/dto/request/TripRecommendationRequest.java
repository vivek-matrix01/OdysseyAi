package com.OdysseyAi.demo.ai.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRecommendationRequest {

    @NotBlank
    @Size(max = 100)
    private String tripType;

    @NotBlank
    @Size(max = 200)
    private String destination;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal budget;

    @NotBlank
    @Size(min = 3, max = 3)
    @Builder.Default
    private String currency = "INR";

    @NotNull
    @Min(1)
    @Max(50)
    private Integer travelers;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Size(max = 1000)
    private String preferences;

    @Builder.Default
    private Boolean includeHotels = false;
}