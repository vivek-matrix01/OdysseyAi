package com.OdysseyAi.demo.travel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripCostEstimateRequest {

    @Min(1)
    private Integer numberOfTravelers;

    @DecimalMin(value = "0.0")
    private BigDecimal nightlyHotelCost;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
}