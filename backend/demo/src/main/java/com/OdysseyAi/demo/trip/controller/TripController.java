package com.OdysseyAi.demo.trip.controller;

import com.OdysseyAi.demo.common.api.ApiResponse;
import com.OdysseyAi.demo.common.enums.TripStatus;
import com.OdysseyAi.demo.trip.dto.request.CreateTripRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateTripRequest;
import com.OdysseyAi.demo.trip.dto.response.PageResponse;
import com.OdysseyAi.demo.trip.dto.response.TripResponse;
import com.OdysseyAi.demo.trip.interfaces.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(
            @Valid @RequestBody CreateTripRequest request) {

        TripResponse trip = tripService.createTrip(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<TripResponse>builder()
                                .status(HttpStatus.CREATED.value())
                                .message("Trip created successfully.")
                                .data(trip)
                                .build()
                );
    }
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TripResponse>>> getMyTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) TripStatus status,
            @RequestParam(required = false) String destination
    ) {

        PageResponse<TripResponse> trips =
                tripService.getMyTrips(
                        page,
                        size,
                        sortBy,
                        direction,
                        status,
                        destination
                );

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<TripResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Trips retrieved successfully.")
                        .data(trips)
                        .build()
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponse>> getMyTrip(
            @PathVariable Long id) {

        TripResponse trip = tripService.getMyTrip(id);

        return ResponseEntity.ok(
                ApiResponse.<TripResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Trip retrieved successfully.")
                        .data(trip)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponse>> updateMyTrip(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTripRequest request) {

        TripResponse trip =
                tripService.updateMyTrip(id, request);

        return ResponseEntity.ok(
                ApiResponse.<TripResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Trip updated successfully.")
                        .data(trip)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMyTrip(
            @PathVariable Long id) {

        tripService.deleteMyTrip(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Trip deleted successfully.")
                        .build()
        );
    }
    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<Void>> startTrip(
            @PathVariable Long id) {

        tripService.startTrip(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Trip started successfully.")
                        .build()
        );
    }
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> completeTrip(
            @PathVariable Long id) {

        tripService.completeTrip(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Trip completed successfully.")
                        .build()
        );
    }
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelTrip(
            @PathVariable Long id) {

        tripService.cancelTrip(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Trip cancelled successfully.")
                        .build()
        );
    }
}