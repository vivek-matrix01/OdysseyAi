package com.OdysseyAi.demo.travel.service;

import com.OdysseyAi.demo.travel.dto.HotelData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class HotelLocationResolver {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${google.maps.api-key}")
    private String googleApiKey;

    /**
     * Resolves latitude and longitude for the selected hotel when they are missing.
     *
     * It keeps the existing hotel data unchanged when coordinates are already present.
     *
     * @param hotel selected hotel
     * @return hotel with coordinates populated when Google can resolve them
     */
    public HotelData resolveCoordinates(HotelData hotel) {

        if (hotel == null) {
            return null;
        }

        // Coordinates already available. Nothing to do.
        if (hotel.getLatitude() != null && hotel.getLongitude() != null) {
            return hotel;
        }

        if (hotel.getName() == null || hotel.getName().isBlank()) {
            return hotel;
        }

        String query = buildSearchQuery(hotel);

        try {

            RestClient client = restClientBuilder
                    .baseUrl("https://places.googleapis.com")
                    .build();

            JsonNode response = client.post()
                    .uri("/v1/places:searchText")
                    .header("X-Goog-Api-Key", googleApiKey)
                    .header(
                            "X-Goog-FieldMask",
                            "places.displayName,places.location,places.formattedAddress"
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                              "textQuery": "%s"
                            }
                            """.formatted(escapeJson(query)))
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                return hotel;
            }

            JsonNode places = response.get("places");

            if (places == null || !places.isArray() || places.isEmpty()) {
                return hotel;
            }

            JsonNode firstPlace = places.get(0);

            JsonNode location = firstPlace.get("location");

            if (location == null) {
                return hotel;
            }

            JsonNode latitudeNode = location.get("latitude");
            JsonNode longitudeNode = location.get("longitude");

            if (latitudeNode == null || longitudeNode == null) {
                return hotel;
            }

            hotel.setLatitude(latitudeNode.asDouble());
            hotel.setLongitude(longitudeNode.asDouble());

            // Fill address only when the original hotel data doesn't have one.
            if ((hotel.getAddress() == null || hotel.getAddress().isBlank())
                    && firstPlace.has("formattedAddress")) {

                hotel.setAddress(firstPlace.get("formattedAddress").asText());
            }

            return hotel;

        } catch (Exception ex) {

            /*
             * Do not fail the entire itinerary just because hotel coordinate
             * resolution failed. The itinerary can still be generated.
             */
            return hotel;
        }
    }

    private String buildSearchQuery(HotelData hotel) {

        StringBuilder query = new StringBuilder();

        query.append(hotel.getName());

        if (hotel.getAddress() != null && !hotel.getAddress().isBlank()) {
            query.append(", ").append(hotel.getAddress());
        }

        return query.toString();
    }

    private String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
