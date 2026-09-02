package com.OdysseyAi.demo.trip.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryDayResponse {

    private Long id;

    private Integer dayNumber;

    private LocalDate date;

    private String notes;

    private List<ActivityResponse> activities;
}