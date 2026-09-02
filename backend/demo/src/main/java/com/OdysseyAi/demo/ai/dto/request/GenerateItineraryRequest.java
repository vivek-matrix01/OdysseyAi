package com.OdysseyAi.demo.ai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateItineraryRequest {

    @NotBlank
    @Size(max = 1000)
    private String preferences;

    @Min(1)
    @Max(30)
    private Integer numberOfDays;

    @Size(max = 2000)
    private String specialRequirements;

    @Pattern(regexp = "^[A-Za-z]{3}$")
    @Builder.Default
    private String currency = "INR";
}