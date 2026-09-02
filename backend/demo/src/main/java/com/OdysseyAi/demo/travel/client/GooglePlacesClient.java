package com.OdysseyAi.demo.travel.client;

import com.OdysseyAi.demo.travel.configuration.GoogleMapsProperties;
import com.OdysseyAi.demo.travel.dto.DestinationData;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.interfaces.PlacesClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GooglePlacesClient implements PlacesClient {

    private final GoogleMapsProperties properties;
    private final ObjectMapper objectMapper;

    private final RestClient restClient =
            RestClient.builder().build();

    @Override
    public DestinationData searchDestination(
            String destination
    ) {

        String response =
                restClient.post()
                        .uri(
                                properties.getPlacesBaseUrl()
                                        + "/places:searchText"
                        )
                        .header(
                                "X-Goog-Api-Key",
                                properties.getApiKey()
                        )
                        .header(
                                "X-Goog-FieldMask",
                                "places.id,"
                                        + "places.displayName,"
                                        + "places.formattedAddress,"
                                        + "places.location"
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                """
                                {
                                  "textQuery": "%s",
                                  "pageSize": 1
                                }
                                """.formatted(
                                        escapeJson(destination)
                                )
                        )
                        .retrieve()
                        .body(String.class);

        try {

            JsonNode places =
                    objectMapper
                            .readTree(response)
                            .path("places");

            if (!places.isArray()
                    || places.isEmpty()) {

                throw new IllegalStateException(
                        "Destination could not be resolved: "
                                + destination
                );
            }

            JsonNode place =
                    places.get(0);

            JsonNode displayName =
                    place.path("displayName");

            JsonNode location =
                    place.path("location");

            return DestinationData.builder()
                    .placeId(
                            place.path("id")
                                    .asText(null)
                    )
                    .name(
                            displayName
                                    .path("text")
                                    .asText(destination)
                    )
                    .formattedAddress(
                            place.path("formattedAddress")
                                    .asText(null)
                    )
                    .latitude(
                            location.path("latitude")
                                    .asDouble()
                    )
                    .longitude(
                            location.path("longitude")
                                    .asDouble()
                    )
                    .build();

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to resolve destination using Google Places.",
                    ex
            );
        }
    }

    @Override
    public List<PlaceData> searchNearby(
            double latitude,
            double longitude,
            String placeType,
            int radiusMeters
    ) {

        String response =
                restClient.post()
                        .uri(
                                properties.getPlacesBaseUrl()
                                        + "/places:searchNearby"
                        )
                        .header(
                                "X-Goog-Api-Key",
                                properties.getApiKey()
                        )
                        .header(
                                "X-Goog-FieldMask",
                                "places.id,"
                                        + "places.displayName,"
                                        + "places.formattedAddress,"
                                        + "places.location,"
                                        + "places.primaryType,"
                                        + "places.rating,"
                                        + "places.userRatingCount,"
                                        + "places.priceLevel,"
                                        + "places.priceRange,"
                                        + "places.photos"
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                """
                                {
                                  "includedTypes": ["%s"],
                                  "maxResultCount": 10,
                                  "locationRestriction": {
                                    "circle": {
                                      "center": {
                                        "latitude": %f,
                                        "longitude": %f
                                      },
                                      "radius": %f
                                    }
                                  },
                                  "rankPreference": "POPULARITY"
                                }
                                """.formatted(
                                        placeType,
                                        latitude,
                                        longitude,
                                        (double) radiusMeters
                                )
                        )
                        .retrieve()
                        .body(String.class);

        return parsePlaces(response);
    }

    @Override
    public PlaceData getPlaceById(
            String placeId
    ) {

        if (placeId == null
                || placeId.isBlank()) {

            throw new IllegalArgumentException(
                    "Place ID is required."
            );
        }

        String response =
                restClient.get()
                        .uri(
                                properties.getPlacesBaseUrl()
                                        + "/places/"
                                        + placeId
                        )
                        .header(
                                "X-Goog-Api-Key",
                                properties.getApiKey()
                        )
                        .header(
                                "X-Goog-FieldMask",
                                "id,"
                                        + "displayName,"
                                        + "formattedAddress,"
                                        + "location,"
                                        + "primaryType,"
                                        + "rating,"
                                        + "userRatingCount,"
                                        + "priceLevel,"
                                        + "priceRange,"
                                        + "photos"
                        )
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(String.class);

        try {

            JsonNode place =
                    objectMapper.readTree(response);

            if (place == null
                    || place.path("id")
                    .asText("")
                    .isBlank()) {

                throw new IllegalStateException(
                        "Google Places returned no place for ID: "
                                + placeId
                );
            }

            return parseSinglePlace(
                    place
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to retrieve Google Place details for ID: "
                            + placeId,
                    ex
            );
        }
    }

    private List<PlaceData> parsePlaces(
            String response
    ) {

        try {

            JsonNode places =
                    objectMapper
                            .readTree(response)
                            .path("places");

            List<PlaceData> result =
                    new ArrayList<>();

            for (JsonNode place :
                    places) {

                result.add(
                        parseSinglePlace(
                                place
                        )
                );
            }

            return result;

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to parse Google Places response.",
                    ex
            );
        }
    }

    private PlaceData parseSinglePlace(
            JsonNode place
    ) {

        JsonNode displayName =
                place.path("displayName");

        JsonNode location =
                place.path("location");

        JsonNode priceRange =
                place.path("priceRange");

        BigDecimal minPrice =
                null;

        BigDecimal maxPrice =
                null;

        if (priceRange.has("startPrice")) {

            minPrice =
                    readPrice(
                            priceRange.path("startPrice")
                    );
        }

        if (priceRange.has("endPrice")) {

            maxPrice =
                    readPrice(
                            priceRange.path("endPrice")
                    );
        }

        List<String> photoNames =
                parsePhotoNames(
                        place.path("photos")
                );

        return PlaceData.builder()
                .id(
                        place.path("id")
                                .asText(null)
                )
                .name(
                        displayName
                                .path("text")
                                .asText(null)
                )
                .address(
                        place.path("formattedAddress")
                                .asText(null)
                )
                .latitude(
                        location.path("latitude")
                                .isMissingNode()
                                ? null
                                : location.path("latitude")
                                .asDouble()
                )
                .longitude(
                        location.path("longitude")
                                .isMissingNode()
                                ? null
                                : location.path("longitude")
                                .asDouble()
                )
                .primaryType(
                        place.path("primaryType")
                                .asText(null)
                )
                .rating(
                        place.has("rating")
                                ? place.path("rating")
                                .asDouble()
                                : null
                )
                .userRatingCount(
                        place.has("userRatingCount")
                                ? place.path("userRatingCount")
                                .asInt()
                                : null
                )
                .priceLevel(
                        place.path("priceLevel")
                                .asText(null)
                )
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .photoNames(photoNames)
                .build();
    }

    private List<String> parsePhotoNames(
            JsonNode photos
    ) {

        List<String> photoNames =
                new ArrayList<>();

        if (!photos.isArray()) {
            return photoNames;
        }

        int limit =
                Math.min(
                        3,
                        photos.size()
                );

        for (int i = 0; i < limit; i++) {

            String name =
                    photos.get(i)
                            .path("name")
                            .asText(null);

            if (name != null
                    && !name.isBlank()) {

                photoNames.add(name);
            }
        }

        return photoNames;
    }

    private BigDecimal readPrice(
            JsonNode priceNode
    ) {

        if (priceNode == null
                || priceNode.isMissingNode()) {

            return null;
        }

        if (priceNode.has("units")) {

            return new BigDecimal(
                    priceNode
                            .path("units")
                            .asText("0")
            );
        }

        return null;
    }

    private String escapeJson(
            String value
    ) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}