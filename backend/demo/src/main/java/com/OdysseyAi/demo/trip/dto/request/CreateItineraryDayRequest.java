package com.OdysseyAi.demo.trip.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateItineraryDayRequest {

    @NotNull
    @Min(1)
    private Integer dayNumber;

    @NotNull
    private LocalDate date;

    @Size(max = 1000)
    private String notes;

    @Valid
    @Builder.Default
    private List<CreateActivityRequest> activities = new ArrayList<>();

}