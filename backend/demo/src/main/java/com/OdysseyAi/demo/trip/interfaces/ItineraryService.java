package com.OdysseyAi.demo.trip.interfaces;

import com.OdysseyAi.demo.trip.dto.request.*;
import com.OdysseyAi.demo.trip.dto.response.ActivityResponse;
import com.OdysseyAi.demo.trip.dto.response.ItineraryDayResponse;
import com.OdysseyAi.demo.trip.dto.response.ItineraryResponse;

public interface ItineraryService {

    ItineraryResponse createItinerary(
            Long tripId,
            CreateItineraryRequest request
    );

    ItineraryResponse getItinerary(Long tripId);
    ItineraryResponse updateItinerary(
            Long tripId,
            UpdateItineraryRequest request
    );
    ItineraryDayResponse addDay(
            Long tripId,
            CreateItineraryDayRequest request
    );

    ItineraryDayResponse updateDay(
            Long tripId,
            Long dayId,
            UpdateItineraryDayRequest request
    );

    void deleteDay(
            Long tripId,
            Long dayId
    );
    ActivityResponse addActivity(
            Long tripId,
            Long dayId,
            CreateActivityRequest request
    );

    ActivityResponse updateActivity(
            Long tripId,
            Long dayId,
            Long activityId,
            UpdateActivityRequest request
    );

    void deleteActivity(
            Long tripId,
            Long dayId,
            Long activityId
    );
}