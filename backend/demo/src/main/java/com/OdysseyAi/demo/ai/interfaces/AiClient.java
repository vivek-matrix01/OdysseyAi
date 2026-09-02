package com.OdysseyAi.demo.ai.interfaces;

import com.OdysseyAi.demo.ai.dto.response.GeneratedItineraryResponse;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;

public interface AiClient {

    GeneratedItineraryResponse generateItinerary(
            String prompt
    );

    TripRecommendationResponse recommendTrip(
            String prompt
    );
}