package com.OdysseyAi.demo.travel.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteData {

    private String origin;

    private String destination;

    private String travelMode;

    private Long durationSeconds;

    private Long distanceMeters;

    /*
     * Google can return a transit fare when available.
     * Null means the fare was not available.
     */
    private BigDecimal fare;

    private String fareCurrency;
}