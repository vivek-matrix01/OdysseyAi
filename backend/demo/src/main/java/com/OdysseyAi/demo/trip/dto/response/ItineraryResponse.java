package com.OdysseyAi.demo.trip.dto.response;

import com.OdysseyAi.demo.common.enums.ItineraryStatus;
import com.OdysseyAi.demo.travel.dto.RouteData;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryResponse {

    private Long id;

    private Long tripId;

    private String summary;

    private ItineraryStatus status;

    private List<ItineraryDayResponse> days;

    @Builder.Default
    private List<RouteData> routes =
            new ArrayList<>();

    @Builder.Default
    private BigDecimal estimatedHotelCost =
            BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal estimatedActivityCost =
            BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal estimatedTransportCost =
            BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal estimatedTotalCost =
            BigDecimal.ZERO;
}