package com.OdysseyAi.demo.trip.interfaces;

import com.OdysseyAi.demo.trip.dto.request.CreateTripRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateTripRequest;
import com.OdysseyAi.demo.trip.dto.response.TripResponse;

import java.util.List;

public interface TripService {

    TripResponse createTrip(CreateTripRequest request);

    List<TripResponse> getMyTrips();

    TripResponse getMyTrip(Long tripId);

    TripResponse updateMyTrip(Long tripId, UpdateTripRequest request);

    void deleteMyTrip(Long tripId);
    void startTrip(Long tripId);

    void completeTrip(Long tripId);

    void cancelTrip(Long tripId);
}
