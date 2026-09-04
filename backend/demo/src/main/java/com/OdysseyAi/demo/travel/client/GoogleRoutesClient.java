package com.OdysseyAi.demo.travel.client;

import com.OdysseyAi.demo.travel.configuration.GoogleMapsProperties;
import com.OdysseyAi.demo.travel.dto.RouteData;
import com.OdysseyAi.demo.travel.interfaces.RoutesClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class GoogleRoutesClient implements RoutesClient {

    private final GoogleMapsProperties properties;
    private final ObjectMapper objectMapper;

    private final RestClient restClient =
            RestClient.builder().build();

    @Override
    public RouteData calculateRoute(
            String originName,
            double originLatitude,
            double originLongitude,
            String destinationName,
            double destinationLatitude,
            double destinationLongitude,
            String travelMode
    ) {

        validateTravelMode(travelMode);

        String response =
                restClient.post()
                        .uri(
                                properties.getRoutesBaseUrl()
                                        + "/directions/v2:computeRoutes"
                        )
                        .header(
                                "X-Goog-Api-Key",
                                properties.getApiKey()
                        )
                        .header(
                                "X-Goog-FieldMask",
                                buildFieldMask(travelMode)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                """
                                {
                                  "origin": {
                                    "location": {
                                      "latLng": {
                                        "latitude": %f,
                                        "longitude": %f
                                      }
                                    }
                                  },
                                  "destination": {
                                    "location": {
                                      "latLng": {
                                        "latitude": %f,
                                        "longitude": %f
                                      }
                                    }
                                  },
                                  "travelMode": "%s"
                                }
                                """.formatted(
                                        originLatitude,
                                        originLongitude,
                                        destinationLatitude,
                                        destinationLongitude,
                                        travelMode
                                )
                        )
                        .retrieve()
                        .body(String.class);

        return parseResponse(
                response,
                originName,
                destinationName,
                travelMode
        );
    }

    private RouteData parseResponse(
            String response,
            String originName,
            String destinationName,
            String travelMode
    ) {

        try {

            JsonNode route =
                    objectMapper
                            .readTree(response)
                            .path("routes")
                            .path(0);

            if (route.isMissingNode()
                    || route.isEmpty()) {

                // Return a fallback/empty RouteData instead of crashing
                return RouteData.builder()
                        .origin(originName)
                        .destination(destinationName)
                        .travelMode(travelMode)
                        .durationSeconds(0L)
                        .distanceMeters(0L)
                        .build();
            }

            String duration =
                    route.path("duration")
                            .asText("0s");

            Long durationSeconds =
                    parseDurationSeconds(duration);

            Long distanceMeters =
                    route.has("distanceMeters")
                            ? route.path("distanceMeters")
                            .asLong()
                            : null;

            BigDecimal fare = null;
            String fareCurrency = null;

            /*
             * Transit fare exists only when Google can determine
             * the fare for the complete transit route.
             */
            if ("TRANSIT".equals(travelMode)
                    && route.has("travelAdvisory")) {

                JsonNode travelAdvisory =
                        route.path("travelAdvisory");

                JsonNode transitFare =
                        travelAdvisory.path("transitFare");

                if (!transitFare.isMissingNode()
                        && !transitFare.isNull()) {

                    if (transitFare.has("units")) {

                        fare =
                                new BigDecimal(
                                        transitFare
                                                .path("units")
                                                .asText("0")
                                );

                        fareCurrency =
                                transitFare
                                        .path("currencyCode")
                                        .asText(null);
                    }
                }
            }

            return RouteData.builder()
                    .origin(originName)
                    .destination(destinationName)
                    .travelMode(travelMode)
                    .durationSeconds(durationSeconds)
                    .distanceMeters(distanceMeters)
                    .fare(fare)
                    .fareCurrency(fareCurrency)
                    .build();

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to parse Google Routes response.",
                    ex
            );
        }
    }

    private String buildFieldMask(
            String travelMode
    ) {

        if ("TRANSIT".equals(travelMode)) {

            return "routes.duration,"
                    + "routes.distanceMeters,"
                    + "routes.travelAdvisory.transitFare";
        }

        return "routes.duration,"
                + "routes.distanceMeters";
    }

    private void validateTravelMode(
            String travelMode
    ) {

        if (!"DRIVE".equals(travelMode)
                && !"TRANSIT".equals(travelMode)) {

            throw new IllegalArgumentException(
                    "Travel mode must be DRIVE or TRANSIT."
            );
        }
    }

    private Long parseDurationSeconds(
            String duration
    ) {

        if (!duration.endsWith("s")) {

            throw new IllegalStateException(
                    "Unsupported Google Routes duration format."
            );
        }

        return Math.round(
                Double.parseDouble(
                        duration.substring(
                                0,
                                duration.length() - 1
                        )
                )
        );
    }
}