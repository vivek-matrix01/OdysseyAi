package com.OdysseyAi.demo.ai.dto.response;

import com.OdysseyAi.demo.trip.dto.response.TripResponse;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmedTripResponse {

    private TripResponse trip;

    private String selectedHotelPropertyToken;

    @Builder.Default
    private List<String> selectedPlaceIds =
            new ArrayList<>();
}