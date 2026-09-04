package com.OdysseyAi.demo.ai.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRecommendationResponse {

    private String destination;

    private String tripType;

    private BigDecimal budget;

    private String currency;

    private Integer travelers;

    private BigDecimal estimatedTotalCost;

    private BigDecimal estimatedTransportCost;

    private BudgetAnalysis budgetAnalysis;

    @Builder.Default
    private List<RecommendedPlaceResponse> places =
            new ArrayList<>();

    @Builder.Default
    private List<RecommendedHotelResponse> hotels =
            new ArrayList<>();

    private String recommendationReason;

    private Double totalDistanceKm;

    private BigDecimal petrolVehicleCost;

    private BigDecimal dieselVehicleCost;

    @Builder.Default
    private List<RouteSegmentResponse> routeSegments =
            new ArrayList<>();

    @Builder.Default
    private List<String> potentialProblems =
            new ArrayList<>();

    private String localCurrency;

    private String localCurrencySymbol;

    private BigDecimal exchangeRateFromInr;

    private BigDecimal localPetrolPricePerLiter;
}