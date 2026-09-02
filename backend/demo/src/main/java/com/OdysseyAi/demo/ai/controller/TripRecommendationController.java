package com.OdysseyAi.demo.ai.controller;

import com.OdysseyAi.demo.ai.dto.request.TripRecommendationRequest;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;
import com.OdysseyAi.demo.ai.interfaces.TripRecommendationService;
import com.OdysseyAi.demo.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/recommendations")
@RequiredArgsConstructor
public class TripRecommendationController {

    private final TripRecommendationService tripRecommendationService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripRecommendationResponse>> recommend(
            @Valid @RequestBody TripRecommendationRequest request
    ) {

        TripRecommendationResponse response =
                tripRecommendationService.recommend(request);

        return ResponseEntity.ok(
                ApiResponse.<TripRecommendationResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Travel recommendations generated successfully.")
                        .data(response)
                        .build()
        );
    }
}