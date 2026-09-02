package com.OdysseyAi.demo.trip.entity;

import com.OdysseyAi.demo.travel.dto.RouteData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class RouteDataListConverter
        implements AttributeConverter<
        List<RouteData>,
        String> {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    private static final TypeReference<List<RouteData>>
            ROUTE_LIST_TYPE =
            new TypeReference<>() {
            };


    @Override
    public String convertToDatabaseColumn(
            List<RouteData> routes
    ) {

        if (routes == null
                || routes.isEmpty()) {

            return "[]";
        }

        try {

            return OBJECT_MAPPER.writeValueAsString(
                    routes
            );

        } catch (JsonProcessingException ex) {

            throw new IllegalArgumentException(
                    "Failed to serialize route data.",
                    ex
            );
        }
    }


    @Override
    public List<RouteData> convertToEntityAttribute(
            String routesJson
    ) {

        if (routesJson == null
                || routesJson.isBlank()) {

            return new ArrayList<>();
        }

        try {

            return OBJECT_MAPPER.readValue(
                    routesJson,
                    ROUTE_LIST_TYPE
            );

        } catch (JsonProcessingException ex) {

            throw new IllegalArgumentException(
                    "Failed to deserialize route data.",
                    ex
            );
        }
    }
}