package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.request.TripRecommendationRequest;
import com.OdysseyAi.demo.ai.dto.response.BudgetAnalysis;
import com.OdysseyAi.demo.ai.dto.response.RecommendedHotelResponse;
import com.OdysseyAi.demo.ai.dto.response.RecommendedPlaceResponse;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class RecommendationCostCalculator {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

    public TripRecommendationResponse calculate(
            TripRecommendationRequest request,
            TripRecommendationResponse recommendation
    ) {

        long hotelNights =
                java.time.temporal.ChronoUnit.DAYS.between(
                        request.getStartDate(),
                        request.getEndDate()
                );

        BigDecimal hotelCost =
                calculateHotelCost(
                        recommendation.getHotels(),
                        hotelNights
                );

        BigDecimal activityCost =
                calculateActivityCost(
                        recommendation.getPlaces()
                );

        int tripDays =
                Math.toIntExact(
                        hotelNights + 1
                );

        BigDecimal foodCost =
                calculateFoodCost(
                        recommendation.getPlaces(),
                        request.getTravelers(),
                        tripDays
                );

        BigDecimal transportCost =
                recommendation.getEstimatedTransportCost() == null
                        ? BigDecimal.ZERO
                        : recommendation.getEstimatedTransportCost();

        BigDecimal totalCost =
                hotelCost
                        .add(foodCost)
                        .add(activityCost)
                        .add(transportCost)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal budget =
                request.getBudget();

        BigDecimal remainingBudget =
                budget.subtract(totalCost)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal usedPercentage =
                calculateBudgetPercentage(
                        totalCost,
                        budget
                );

        boolean withinBudget =
                totalCost.compareTo(budget) <= 0;

        BudgetAnalysis analysis =
                BudgetAnalysis.builder()
                        .budget(money(budget))
                        .estimatedTotalCost(money(totalCost))
                        .remainingBudget(money(remainingBudget))
                        .budgetUsedPercentage(usedPercentage)
                        .withinBudget(withinBudget)
                        .message(
                                withinBudget
                                        ? "The current recommendation estimate is within your budget."
                                        : "The current recommendation estimate exceeds your budget."
                        )
                        .build();

        recommendation.setEstimatedTotalCost(
                money(totalCost)
        );

        recommendation.setBudgetAnalysis(
                analysis
        );

        return recommendation;
    }

    private BigDecimal calculateHotelCost(
            List<RecommendedHotelResponse> hotels,
            long hotelNights
    ) {

        if (hotels == null
                || hotels.isEmpty()) {

            return ZERO;
        }

        RecommendedHotelResponse hotel =
                hotels.getFirst();

        /*
         * Prefer the provider's total price for the exact stay.
         */
        if (hotel.getTotalCost() != null
                && hotel.getTotalCost().signum() >= 0) {

            return hotel.getTotalCost();
        }

        /*
         * Fallback to nightly price × nights.
         */
        if (hotel.getEstimatedNightlyCost() != null
                && hotelNights > 0) {

            return hotel.getEstimatedNightlyCost()
                    .multiply(
                            BigDecimal.valueOf(
                                    hotelNights
                            )
                    );
        }

        return ZERO;
    }
    private BigDecimal calculateActivityCost(
            List<RecommendedPlaceResponse> places
    ) {

        if (places == null
                || places.isEmpty()) {

            return ZERO;
        }

        BigDecimal total =
                ZERO;

        for (RecommendedPlaceResponse place :
                places) {

            if (isFoodPlace(place)) {
                continue;
            }

            if (place.getEstimatedCost() == null) {
                continue;
            }

            total =
                    total.add(
                            place.getEstimatedCost()
                    );
        }

        return total;
    }

    private BigDecimal calculateFoodCost(
            List<RecommendedPlaceResponse> places,
            int travelers,
            int tripDays
    ) {

        if (places == null
                || places.isEmpty()) {

            return ZERO;
        }

        BigDecimal total =
                ZERO;

        int foodPlaceCount =
                0;

        for (RecommendedPlaceResponse place :
                places) {

            if (!isFoodPlace(place)) {
                continue;
            }

            if (place.getEstimatedCost() == null) {
                continue;
            }

            total =
                    total.add(
                            place.getEstimatedCost()
                    );

            foodPlaceCount++;
        }

        if (foodPlaceCount == 0) {
            return ZERO;
        }

        BigDecimal averageMealCost =
                total.divide(
                        BigDecimal.valueOf(
                                foodPlaceCount
                        ),
                        2,
                        RoundingMode.HALF_UP
                );

        /*
         * Application assumption:
         * 3 meals per traveler per day.
         */
        return averageMealCost
                .multiply(
                        BigDecimal.valueOf(3)
                )
                .multiply(
                        BigDecimal.valueOf(travelers)
                )
                .multiply(
                        BigDecimal.valueOf(tripDays)
                );
    }

    private boolean isFoodPlace(
            RecommendedPlaceResponse place
    ) {

        if (place.getCategory() == null) {
            return false;
        }

        return place.getCategory()
                .toLowerCase()
                .contains("restaurant");
    }

    private BigDecimal calculateBudgetPercentage(
            BigDecimal total,
            BigDecimal budget
    ) {

        if (budget == null
                || budget.signum() == 0) {

            return ZERO;
        }

        return total
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .divide(
                        budget,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal money(
            BigDecimal value
    ) {

        if (value == null) {
            return ZERO;
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}