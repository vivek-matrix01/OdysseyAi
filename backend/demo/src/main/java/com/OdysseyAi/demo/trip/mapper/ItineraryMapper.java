package com.OdysseyAi.demo.trip.mapper;

import com.OdysseyAi.demo.trip.dto.request.CreateActivityRequest;
import com.OdysseyAi.demo.trip.dto.request.CreateItineraryDayRequest;
import com.OdysseyAi.demo.trip.dto.request.CreateItineraryRequest;
import com.OdysseyAi.demo.trip.dto.response.ActivityResponse;
import com.OdysseyAi.demo.trip.dto.response.ItineraryDayResponse;
import com.OdysseyAi.demo.trip.dto.response.ItineraryResponse;
import com.OdysseyAi.demo.trip.entity.Activity;
import com.OdysseyAi.demo.trip.entity.Itinerary;
import com.OdysseyAi.demo.trip.entity.ItineraryDay;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItineraryMapper {


    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "trip",
            ignore = true
    )
    @Mapping(
            target = "status",
            ignore = true
    )
    @Mapping(
            target = "days",
            ignore = true
    )
    @Mapping(
            target = "routes",
            ignore = true
    )
    @Mapping(
            target = "estimatedHotelCost",
            ignore = true
    )
    @Mapping(
            target = "estimatedActivityCost",
            ignore = true
    )
    @Mapping(
            target = "estimatedTransportCost",
            ignore = true
    )
    @Mapping(
            target = "estimatedTotalCost",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            ignore = true
    )
    @Mapping(
            target = "updatedAt",
            ignore = true
    )
    Itinerary toEntity(
            CreateItineraryRequest request
    );


    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "itinerary",
            ignore = true
    )
    @Mapping(
            target = "activities",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            ignore = true
    )
    @Mapping(
            target = "updatedAt",
            ignore = true
    )
    ItineraryDay toEntity(
            CreateItineraryDayRequest request
    );

    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "itineraryDay",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            ignore = true
    )
    @Mapping(
            target = "updatedAt",
            ignore = true
    )
    Activity toEntity(
            CreateActivityRequest request
    );

    @Mapping(
            target = "tripId",
            source = "trip.id"
    )
    @Mapping(
            target = "routes",
            source = "routes"
    )
    @Mapping(
            target = "estimatedHotelCost",
            source = "estimatedHotelCost"
    )
    @Mapping(
            target = "estimatedActivityCost",
            source = "estimatedActivityCost"
    )
    @Mapping(
            target = "estimatedTransportCost",
            source = "estimatedTransportCost"
    )
    @Mapping(
            target = "estimatedTotalCost",
            source = "estimatedTotalCost"
    )
    ItineraryResponse toResponse(
            Itinerary itinerary
    );



    ItineraryDayResponse toResponse(
            ItineraryDay day
    );



    ActivityResponse toResponse(
            Activity activity
    );
}