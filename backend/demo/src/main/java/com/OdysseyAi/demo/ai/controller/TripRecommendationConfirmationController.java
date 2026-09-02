package com.OdysseyAi.demo.ai.controller;

import com.OdysseyAi.demo.ai.dto.request.ConfirmTripRecommendationRequest;
import com.OdysseyAi.demo.ai.dto.response.ConfirmedTripResponse;
import com.OdysseyAi.demo.ai.service.TripRecommendationConfirmationService;
import com.OdysseyAi.demo.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/recommendations")
@RequiredArgsConstructor
public class TripRecommendationConfirmationController {

    private final TripRecommendationConfirmationService
            confirmationService;

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<ConfirmedTripResponse>> confirm(
            @Valid @RequestBody
            ConfirmTripRecommendationRequest request
    ) {

        ConfirmedTripResponse response =
                confirmationService.confirm(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse
                                .<ConfirmedTripResponse>builder()
                                .status(
                                        HttpStatus.CREATED.value()
                                )
                                .message(
                                        "Trip created from recommendation successfully."
                                )
                                .data(response)
                                .build()
                );
    }
}