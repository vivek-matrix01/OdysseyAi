package com.OdysseyAi.demo.trip.controller;

import com.OdysseyAi.demo.common.api.ApiResponse;
import com.OdysseyAi.demo.trip.dto.request.*;
import com.OdysseyAi.demo.trip.dto.response.ActivityResponse;
import com.OdysseyAi.demo.trip.dto.response.ItineraryDayResponse;
import com.OdysseyAi.demo.trip.dto.response.ItineraryResponse;
import com.OdysseyAi.demo.trip.interfaces.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips/{tripId}/itinerary")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    @PostMapping
    public ResponseEntity<ApiResponse<ItineraryResponse>> createItinerary(
            @PathVariable Long tripId,
            @Valid @RequestBody CreateItineraryRequest request
    ) {

        ItineraryResponse response =
                itineraryService.createItinerary(tripId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<ItineraryResponse>builder()
                                .status(HttpStatus.CREATED.value())
                                .message("Itinerary created successfully.")
                                .data(response)
                                .build()
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ItineraryResponse>> getItinerary(
            @PathVariable Long tripId
    ) {

        ItineraryResponse response =
                itineraryService.getItinerary(tripId);

        return ResponseEntity.ok(
                ApiResponse.<ItineraryResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Itinerary retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ItineraryResponse>> updateItinerary(
            @PathVariable Long tripId,
            @Valid @RequestBody UpdateItineraryRequest request
    ) {

        ItineraryResponse response =
                itineraryService.updateItinerary(
                        tripId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.<ItineraryResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Itinerary updated successfully.")
                        .data(response)
                        .build()
        );
    }
    @PostMapping("/days")
    public ResponseEntity<ApiResponse<ItineraryDayResponse>> addDay(
            @PathVariable Long tripId,
            @Valid @RequestBody CreateItineraryDayRequest request
    ) {

        ItineraryDayResponse response =
                itineraryService.addDay(tripId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<ItineraryDayResponse>builder()
                                .status(HttpStatus.CREATED.value())
                                .message("Itinerary day added successfully.")
                                .data(response)
                                .build()
                );
    }
    @PutMapping("/days/{dayId}")
    public ResponseEntity<ApiResponse<ItineraryDayResponse>> updateDay(
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @Valid @RequestBody UpdateItineraryDayRequest request
    ) {

        ItineraryDayResponse response =
                itineraryService.updateDay(
                        tripId,
                        dayId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.<ItineraryDayResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Itinerary day updated successfully.")
                        .data(response)
                        .build()
        );
    }
    @DeleteMapping("/days/{dayId}")
    public ResponseEntity<ApiResponse<Void>> deleteDay(
            @PathVariable Long tripId,
            @PathVariable Long dayId
    ) {

        itineraryService.deleteDay(tripId, dayId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Itinerary day deleted successfully.")
                        .build()
        );
    }
    @PostMapping("/days/{dayId}/activities")
    public ResponseEntity<ApiResponse<ActivityResponse>> addActivity(
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @Valid @RequestBody CreateActivityRequest request
    ) {

        ActivityResponse response =
                itineraryService.addActivity(
                        tripId,
                        dayId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<ActivityResponse>builder()
                                .status(HttpStatus.CREATED.value())
                                .message("Activity added successfully.")
                                .data(response)
                                .build()
                );
    }
    @PutMapping("/days/{dayId}/activities/{activityId}")
    public ResponseEntity<ApiResponse<ActivityResponse>> updateActivity(
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable Long activityId,
            @Valid @RequestBody UpdateActivityRequest request
    ) {

        ActivityResponse response =
                itineraryService.updateActivity(
                        tripId,
                        dayId,
                        activityId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.<ActivityResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Activity updated successfully.")
                        .data(response)
                        .build()
        );
    }
    @DeleteMapping("/days/{dayId}/activities/{activityId}")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable Long activityId
    ) {

        itineraryService.deleteActivity(
                tripId,
                dayId,
                activityId
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Activity deleted successfully.")
                        .build()
        );
    }
}