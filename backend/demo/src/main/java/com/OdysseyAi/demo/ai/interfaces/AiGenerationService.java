package com.OdysseyAi.demo.ai.interfaces;

import com.OdysseyAi.demo.ai.dto.request.GenerateItineraryRequest;
import com.OdysseyAi.demo.ai.dto.request.GenerateSelectedItineraryRequest;
import com.OdysseyAi.demo.trip.dto.response.ItineraryResponse;
import org.springframework.transaction.annotation.Transactional;

public interface AiGenerationService {

    ItineraryResponse generateItinerary(
            Long tripId,
            GenerateItineraryRequest request
    );


    ItineraryResponse generateSelectedItinerary(
            GenerateSelectedItineraryRequest request
    );
}