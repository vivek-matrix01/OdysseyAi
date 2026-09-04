package com.OdysseyAi.demo.ai.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteSegmentResponse {

    private String originName;

    private String destinationName;

    private Double distanceKm;

    private Long durationMinutes;

    private BigDecimal estimatedCost;
}
