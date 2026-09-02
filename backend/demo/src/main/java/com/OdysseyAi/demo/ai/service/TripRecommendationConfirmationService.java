package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.request.ConfirmTripRecommendationRequest;
import com.OdysseyAi.demo.ai.dto.response.ConfirmedTripResponse;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.trip.dto.request.CreateTripRequest;
import com.OdysseyAi.demo.trip.dto.response.TripResponse;
import com.OdysseyAi.demo.trip.interfaces.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripRecommendationConfirmationService {

    private final TripService tripService;

    public ConfirmedTripResponse confirm(
            ConfirmTripRecommendationRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    "Confirmation request is required."
            );
        }

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new BadRequestException(
                    "Trip end date cannot be before the start date."
            );
        }

        CreateTripRequest createTripRequest =
                new CreateTripRequest();

        createTripRequest.setTitle(
                request.getTitle()
        );

        createTripRequest.setDescription(
                request.getDescription()
        );

        createTripRequest.setDestination(
                request.getDestination()
        );

        createTripRequest.setStartDate(
                request.getStartDate()
        );

        createTripRequest.setEndDate(
                request.getEndDate()
        );

        TripResponse trip =
                tripService.createTrip(
                        createTripRequest
                );

        return ConfirmedTripResponse.builder()
                .trip(trip)
                .selectedHotelPropertyToken(
                        request.getSelectedHotelPropertyToken()
                )
                .selectedPlaceIds(
                        request.getSelectedPlaceIds()
                )
                .build();
    }
}