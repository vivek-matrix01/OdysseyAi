package com.OdysseyAi.demo.trip.dto.request;


import com.OdysseyAi.demo.common.enums.ActivityType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateActivityRequest {

    @NotBlank
    @Size(max = 150)
    private String title;

    @Size(max = 1000)
    private String description;

    private LocalTime startTime;

    private LocalTime endTime;

    @Size(max = 255)
    private String location;

    @DecimalMin(value = "0.0")
    private BigDecimal estimatedCost;

    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    private ActivityType type;
}