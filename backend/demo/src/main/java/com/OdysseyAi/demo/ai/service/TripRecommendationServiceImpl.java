package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.request.TripRecommendationRequest;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;
import com.OdysseyAi.demo.ai.interfaces.AiClient;
import com.OdysseyAi.demo.ai.interfaces.TripRecommendationService;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.travel.dto.HotelData;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.dto.TravelContext;
import com.OdysseyAi.demo.travel.interfaces.HotelClient;
import com.OdysseyAi.demo.travel.interfaces.TravelDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripRecommendationServiceImpl
        implements TripRecommendationService {

    private final TravelDataService travelDataService;
    private final HotelClient hotelClient;

    private final RecommendationPromptBuilder recommendationPromptBuilder;

    private final HotelRecommendationEnricher hotelRecommendationEnricher;
    private final RecommendationResponseValidator recommendationResponseValidator;
    private final PlaceRecommendationEnricher placeRecommendationEnricher;

    private final RecommendationTransportEstimator
            recommendationTransportEstimator;

    private final RecommendationCostCalculator
            recommendationCostCalculator;

    private final AiClient aiClient;

    @Override
    public TripRecommendationResponse recommend(
            TripRecommendationRequest request
    ) {

        validateRequest(request);

        TravelContext travelContext =
                travelDataService.buildTravelContext(
                        request.getDestination(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getCurrency()
                );

        List<PlaceData> places =
                travelContext.getPlaces();

        List<HotelData> hotels =
                List.of();

        if (Boolean.TRUE.equals(
                request.getIncludeHotels()
        )) {

            try {

                hotels =
                        hotelClient.searchHotels(
                                request.getDestination(),
                                request.getStartDate(),
                                request.getEndDate(),
                                request.getTravelers(),
                                request.getCurrency()
                        );

            } catch (Exception ex) {

                hotels = List.of();
            }
        }

        String prompt =
                recommendationPromptBuilder.buildPrompt(
                        request,
                        travelContext,
                        hotels
                );

        TripRecommendationResponse recommendation =
                aiClient.recommendTrip(
                        prompt
                );
        recommendationResponseValidator.validate(
                recommendation,
                places,
                hotels
        );

        recommendation =
                hotelRecommendationEnricher.enrich(
                        recommendation,
                        hotels
                );

        recommendation =
                placeRecommendationEnricher.enrich(
                        recommendation,
                        places
                );

        /*
         * Calculate transport only after Groq has selected
         * the recommended places.
         */
        BigDecimal transportCost =
                recommendationTransportEstimator.estimate(
                        request,
                        recommendation,
                        places
                );

        recommendation.setEstimatedTransportCost(
                transportCost
        );

        recommendation =
                recommendationCostCalculator.calculate(
                        request,
                        recommendation
                );

        return recommendation;
    }

    private void validateRequest(
            TripRecommendationRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    "Recommendation request is required."
            );
        }

        if (request.getTripType() == null
                || request.getTripType().isBlank()) {

            throw new BadRequestException(
                    "Trip type is required."
            );
        }

        if (request.getDestination() == null
                || request.getDestination().isBlank()) {

            throw new BadRequestException(
                    "Destination is required."
            );
        }

        if (request.getBudget() == null
                || request.getBudget().signum() < 0) {

            throw new BadRequestException(
                    "Budget cannot be negative."
            );
        }

        if (request.getTravelers() == null
                || request.getTravelers() < 1) {

            throw new BadRequestException(
                    "Travelers must be at least 1."
            );
        }

        if (request.getStartDate() == null
                || request.getEndDate() == null) {

            throw new BadRequestException(
                    "Travel dates are required."
            );
        }

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new BadRequestException(
                    "End date cannot be before start date."
            );
        }

        long numberOfDays =
                ChronoUnit.DAYS.between(
                        request.getStartDate(),
                        request.getEndDate()
                ) + 1;

        if (numberOfDays > 30) {

            throw new BadRequestException(
                    "Trip duration cannot exceed 30 days."
            );
        }

        if (request.getCurrency() == null
                || !request.getCurrency()
                .matches("[A-Za-z]{3}")) {

            throw new BadRequestException(
                    "Currency must be a valid 3-letter code."
            );
        }
    }
}