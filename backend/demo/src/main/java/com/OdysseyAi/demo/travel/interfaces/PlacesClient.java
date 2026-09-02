package com.OdysseyAi.demo.travel.interfaces;

import com.OdysseyAi.demo.travel.dto.DestinationData;
import com.OdysseyAi.demo.travel.dto.PlaceData;

import java.util.List;

public interface PlacesClient {

    List<PlaceData> searchNearby(
            double latitude,
            double longitude,
            String placeType,
            int radiusMeters
    );

    DestinationData searchDestination(
            String destination
    );

    PlaceData getPlaceById(
            String placeId
    );
}