package com.OdysseyAi.demo.ai.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedItineraryResponse {

    @Builder.Default
    private List<GeneratedItineraryDayResponse> days = new ArrayList<>();
}