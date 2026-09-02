package com.OdysseyAi.demo.ai.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedItineraryDayResponse {

    private Integer dayNumber;

    private LocalDate date;

    private String notes;

    @Builder.Default
    private List<GeneratedActivityResponse> activities = new ArrayList<>();
}