package com.OdysseyAi.demo.trip.dto.request;



import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateTripRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 100)
    private String destination;

    @FutureOrPresent
    @NotNull
    @NotNull
    private LocalDate startDate;

    @FutureOrPresent
    @NotNull
    @NotNull
    private LocalDate endDate;
}
