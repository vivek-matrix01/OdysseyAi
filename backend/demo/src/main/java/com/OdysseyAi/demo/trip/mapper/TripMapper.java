package com.OdysseyAi.demo.trip.mapper;


import com.OdysseyAi.demo.trip.dto.request.CreateTripRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateTripRequest;
import com.OdysseyAi.demo.trip.dto.response.TripResponse;

import com.OdysseyAi.demo.trip.enitity.Trip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Trip toEntity(CreateTripRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateTripRequest request, @MappingTarget Trip trip);

    TripResponse toResponse(Trip trip);
}
