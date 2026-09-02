package com.OdysseyAi.demo.trip.interfaces;

import com.OdysseyAi.demo.common.enums.TripStatus;
import com.OdysseyAi.demo.trip.dto.request.CreateTripRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateTripRequest;
import com.OdysseyAi.demo.trip.dto.response.PageResponse;
import com.OdysseyAi.demo.trip.dto.response.TripResponse;

import java.util.List;

public interface TripService {

    TripResponse createTrip(CreateTripRequest request);

    PageResponse<TripResponse> getMyTrips(
            int page,
            int size,
            String sortBy,
            String direction,
            TripStatus status,
            String destination
    );

    TripResponse getMyTrip(Long tripId);

    TripResponse updateMyTrip(Long tripId, UpdateTripRequest request);

    void deleteMyTrip(Long tripId);
    void startTrip(Long tripId);

    void completeTrip(Long tripId);

    void cancelTrip(Long tripId);
}
