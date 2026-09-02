package com.OdysseyAi.demo.trip.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItineraryRequest {

    @Size(max = 2000)
    private String summary;

    @Valid
    private List<CreateItineraryDayRequest> days;
}