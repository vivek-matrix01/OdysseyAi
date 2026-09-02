package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.response.RecommendedHotelResponse;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class RecommendationBudgetValidator {

    public TripRecommendationResponse validate(
            TripRecommendationResponse recommendation,
            int tripNights
    ) {

        if (recommendation == null) {
            return null;
        }

        if (recommendation.getHotels() == null
                || recommendation.getHotels().isEmpty()) {

            return recommendation;
        }

        BigDecimal budget =
                recommendation.getBudget();

        if (budget == null
                || budget.signum() <= 0) {

            return recommendation;
        }

        /*
         * Calculate the maximum hotel amount that can reasonably
         * fit inside the total trip budget.
         *
         * For now we reserve 50% of the total budget for
         * accommodation.
         *
         * This is an application rule, not an external API value.
         */
        BigDecimal hotelBudget =
                budget.multiply(
                        BigDecimal.valueOf(0.50)
                );

        List<RecommendedHotelResponse> validHotels =
                new ArrayList<>();

        for (RecommendedHotelResponse hotel :
                recommendation.getHotels()) {

            if (hotel.getEstimatedNightlyCost() == null) {
                continue;
            }

            BigDecimal totalHotelCost =
                    hotel.getEstimatedNightlyCost()
                            .multiply(
                                    BigDecimal.valueOf(
                                            tripNights
                                    )
                            );

            if (totalHotelCost.compareTo(
                    hotelBudget
            ) <= 0) {

                validHotels.add(hotel);
            }
        }

        /*
         * Cheapest suitable hotels first.
         */
        validHotels.sort(
                Comparator.comparing(
                        RecommendedHotelResponse
                                ::getEstimatedNightlyCost
                )
        );

        recommendation.setHotels(
                validHotels
        );

        return recommendation;
    }
}