package com.OdysseyAi.demo.travel.controller;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.interfaces.AuthenticationFacade;
import com.OdysseyAi.demo.common.api.ApiResponse;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.common.exception.customexception.ResourceNotFoundException;
import com.OdysseyAi.demo.travel.dto.TripCostEstimate;
import com.OdysseyAi.demo.travel.dto.TripCostEstimateRequest;
import com.OdysseyAi.demo.travel.dto.TravelContext;
import com.OdysseyAi.demo.travel.interfaces.TravelDataService;
import com.OdysseyAi.demo.travel.service.TripCostEstimator;
import com.OdysseyAi.demo.trip.entity.Itinerary;
import com.OdysseyAi.demo.trip.entity.Trip;
import com.OdysseyAi.demo.trip.interfaces.TripAccessService;
import com.OdysseyAi.demo.trip.repository.ItineraryRepository;
import com.OdysseyAi.demo.trip.repository.TripRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips/{tripId}/cost-estimate")
@RequiredArgsConstructor
public class TripCostEstimateController {

    private final TripRepository tripRepository;
    private final ItineraryRepository itineraryRepository;
    private final AuthenticationFacade authenticationFacade;
    private final TripAccessService tripAccessService;
    private final TravelDataService travelDataService;
    private final TripCostEstimator tripCostEstimator;

    @PostMapping
    public ResponseEntity<ApiResponse<TripCostEstimate>> estimateCost(
            @PathVariable Long tripId,
            @Valid @RequestBody TripCostEstimateRequest request
    ) {

        User currentUser =
                authenticationFacade.getCurrentUser();

        Trip trip =
                tripRepository.findById(tripId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Trip not found."
                                )
                        );

        tripAccessService.requireViewer(
                trip,
                currentUser
        );

        Itinerary itinerary =
                itineraryRepository.findByTrip(trip)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary not found."
                                )
                        );

        if (request.getNumberOfTravelers() == null
                || request.getNumberOfTravelers() < 1) {

            throw new BadRequestException(
                    "Number of travelers must be at least 1."
            );
        }

        TravelContext travelContext =
                travelDataService.buildTravelContext(
                        trip,
                        request.getCurrency()
                );

        TripCostEstimate estimate =
                tripCostEstimator.estimate(
                        trip,
                        itinerary,
                        travelContext,
                        request.getNumberOfTravelers(),
                        request.getNightlyHotelCost()
                );

        return ResponseEntity.ok(
                ApiResponse.<TripCostEstimate>builder()
                        .status(HttpStatus.OK.value())
                        .message(
                                "Trip cost estimated successfully."
                        )
                        .data(estimate)
                        .build()
        );
    }
}