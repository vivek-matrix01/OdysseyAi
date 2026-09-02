package com.OdysseyAi.demo.trip.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItineraryDayRequest {

    @NotNull
    private LocalDate date;

    @Size(max = 1000)
    private String notes;
}