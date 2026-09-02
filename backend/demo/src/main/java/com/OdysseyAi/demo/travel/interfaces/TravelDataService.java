package com.OdysseyAi.demo.travel.interfaces;

import com.OdysseyAi.demo.travel.dto.TravelContext;
import com.OdysseyAi.demo.trip.entity.Trip;

import java.time.LocalDate;

public interface TravelDataService {

    TravelContext buildTravelContext(
            Trip trip,
            String targetCurrency
    );

    TravelContext buildTravelContext(
            String destination,
            LocalDate startDate,
            LocalDate endDate,
            String targetCurrency
    );
    TravelContext buildTravelContextWithoutRoutes(
            Trip trip,
            String targetCurrency
    );

    TravelContext buildTravelContextWithoutRoutes(
            String destination,
            LocalDate startDate,
            LocalDate endDate,
            String targetCurrency
    );
}