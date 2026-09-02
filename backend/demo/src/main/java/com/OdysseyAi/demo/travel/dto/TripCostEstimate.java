package com.OdysseyAi.demo.travel.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripCostEstimate {

    private String currency;

    private Integer numberOfTravelers;

    private Integer tripDays;

    private Integer hotelNights;

    private BigDecimal nightlyHotelCost;

    private BigDecimal dailyMealCostPerTraveler;

    private BigDecimal estimatedTransportDistanceKm;

    private BigDecimal estimatedTransportCost;

    private BigDecimal accommodationCost;

    private BigDecimal foodCost;

    private BigDecimal transportCost;

    private BigDecimal activityCost;

    private BigDecimal otherCost;

    private BigDecimal totalCost;

    private BigDecimal perTravelerCost;
}