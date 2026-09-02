package com.OdysseyAi.demo.ai.controller;

import com.OdysseyAi.demo.ai.dto.request.GenerateItineraryRequest;
import com.OdysseyAi.demo.ai.dto.request.GenerateSelectedItineraryRequest;
import com.OdysseyAi.demo.ai.interfaces.AiGenerationService;
import com.OdysseyAi.demo.common.api.ApiResponse;
import com.OdysseyAi.demo.trip.dto.response.ItineraryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/trips")
@RequiredArgsConstructor
public class AiController {

    private final AiGenerationService aiGenerationService;

    @PostMapping("/{tripId}/itinerary/generate")
    public ResponseEntity<ApiResponse<ItineraryResponse>> generateItinerary(
            @PathVariable Long tripId,
            @Valid @RequestBody GenerateItineraryRequest request
    ) {

        ItineraryResponse response =
                aiGenerationService.generateItinerary(
                        tripId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.<ItineraryResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Itinerary generated successfully.")
                        .data(response)
                        .build()
        );
    }
    @PostMapping("/itinerary/from-selection")
    public ResponseEntity<ApiResponse<ItineraryResponse>> generateSelectedItinerary(
            @Valid
            @RequestBody
            GenerateSelectedItineraryRequest request
    ) {

        ItineraryResponse itinerary =
                aiGenerationService.generateSelectedItinerary(
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.<ItineraryResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message(
                                "Itinerary generated from selected recommendations successfully."
                        )
                        .data(itinerary)
                        .build()
        );
    }
}