package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.response.GeneratedItineraryResponse;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.travel.dto.HotelData;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SelectedItineraryValidator {

    private final ObjectMapper objectMapper;

    public void validate(
            GeneratedItineraryResponse generatedResponse,
            List<PlaceData> selectedPlaces,
            HotelData selectedHotel
    ) {

        if (generatedResponse == null) {
            throw new BadRequestException(
                    "AI returned an empty itinerary."
            );
        }

        Set<String> allowedLocations =
                new HashSet<>();

        if (selectedPlaces != null) {

            for (PlaceData place :
                    selectedPlaces) {

                if (place == null) {
                    continue;
                }

                if (place.getName() != null
                        && !place.getName().isBlank()) {

                    allowedLocations.add(
                            normalize(place.getName())
                    );
                }
            }
        }

        if (selectedHotel != null
                && selectedHotel.getName() != null
                && !selectedHotel.getName().isBlank()) {

            allowedLocations.add(
                    normalize(selectedHotel.getName())
            );
        }

        if (allowedLocations.isEmpty()) {
            return;
        }

        JsonNode root;

        try {

            root =
                    objectMapper.valueToTree(
                            generatedResponse
                    );

        } catch (IllegalArgumentException ex) {

            throw new BadRequestException(
                    "Unable to validate the generated itinerary."
            );
        }

        JsonNode days =
                root.path("days");

        if (!days.isArray()) {

            throw new BadRequestException(
                    "Generated itinerary contains no valid days."
            );
        }

        for (JsonNode day :
                days) {

            JsonNode activities =
                    day.path("activities");

            if (!activities.isArray()) {
                continue;
            }

            for (JsonNode activity :
                    activities) {

                String location =
                        activity.path("location")
                                .asText("");

                if (location.isBlank()) {
                    continue;
                }

                if (!isAllowedLocation(
                        location,
                        allowedLocations
                )) {

                    throw new BadRequestException(
                            "AI generated an activity at an unselected location: "
                                    + location
                    );
                }
            }
        }
    }

    private boolean isAllowedLocation(
            String location,
            Set<String> allowedLocations
    ) {

        String normalizedLocation =
                normalize(location);

        for (String allowedLocation :
                allowedLocations) {

            if (normalizedLocation.equals(
                    allowedLocation
            )) {
                return true;
            }

            /*
             * Allow small naming variations.
             */
            if (normalizedLocation.contains(
                    allowedLocation
            )
                    || allowedLocation.contains(
                    normalizedLocation
            )) {

                return true;
            }
        }

        return false;
    }

    private String normalize(
            String value
    ) {

        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll(
                        "\\s+",
                        " "
                );
    }
}