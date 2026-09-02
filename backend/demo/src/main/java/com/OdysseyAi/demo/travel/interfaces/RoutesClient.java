package com.OdysseyAi.demo.travel.interfaces;

import com.OdysseyAi.demo.travel.dto.RouteData;

public interface RoutesClient {

    RouteData calculateRoute(
            String originName,
            double originLatitude,
            double originLongitude,
            String destinationName,
            double destinationLatitude,
            double destinationLongitude,
            String travelMode
    );
}