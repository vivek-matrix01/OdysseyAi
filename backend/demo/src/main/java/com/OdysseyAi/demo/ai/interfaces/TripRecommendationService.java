package com.OdysseyAi.demo.ai.interfaces;

import com.OdysseyAi.demo.ai.dto.request.TripRecommendationRequest;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;

public interface TripRecommendationService {

    TripRecommendationResponse recommend(
            TripRecommendationRequest request
    );
}