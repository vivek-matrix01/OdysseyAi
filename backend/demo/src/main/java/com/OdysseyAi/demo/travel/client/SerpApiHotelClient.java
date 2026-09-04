package com.OdysseyAi.demo.travel.client;

import com.OdysseyAi.demo.travel.configuration.SerpApiProperties;
import com.OdysseyAi.demo.travel.dto.HotelData;
import com.OdysseyAi.demo.travel.interfaces.HotelClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SerpApiHotelClient implements HotelClient {

    private final SerpApiProperties properties;
    private final ObjectMapper objectMapper;

    private final RestClient restClient =
            RestClient.builder().build();

    @Override
    public List<HotelData> searchHotels(
            String destination,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int adults,
            String currency
    ) {

        String response =
                restClient.get()
                        .uri(uriBuilder ->
                                uriBuilder
                                        .scheme("https")
                                        .host("serpapi.com")
                                        .path("/search.json")
                                        .queryParam(
                                                "engine",
                                                "google_hotels"
                                        )
                                        .queryParam(
                                                "q",
                                                destination
                                        )
                                        .queryParam(
                                                "check_in_date",
                                                checkInDate
                                        )
                                        .queryParam(
                                                "check_out_date",
                                                checkOutDate
                                        )
                                        .queryParam(
                                                "adults",
                                                adults
                                        )
                                        .queryParam(
                                                "children",
                                                0
                                        )
                                        .queryParam(
                                                "currency",
                                                currency.toUpperCase()
                                        )
                                        .queryParam(
                                                "hl",
                                                "en"
                                        )
                                        .queryParam(
                                                "api_key",
                                                properties.getApiKey()
                                        )
                                        .build()
                        )
                        .retrieve()
                        .body(String.class);

        return parseHotels(response);
    }

    private List<HotelData> parseHotels(
            String response
    ) {

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            if (root.has("error")) {

                String errorMsg =
                        root.path("error")
                                .asText("SerpApi returned an error.");
                String lower = errorMsg.toLowerCase();

                if (lower.contains("location")
                        || lower.contains("not found")
                        || lower.contains("couldn't find")
                        || lower.contains("unknown location")
                        || lower.contains("invalid location")
                        || lower.contains("no results")) {

                    throw new com.OdysseyAi.demo.common.exception.customexception.UnsupportedLocationException(
                            "Sorry, we do not provide services at this location."
                    );
                }

                throw new IllegalStateException(errorMsg);
            }

            JsonNode propertiesNode =
                    root.path("properties");

            List<HotelData> hotels =
                    new ArrayList<>();

            if (!propertiesNode.isArray()) {
                return hotels;
            }

            /*
             * Limit the result we keep in our application.
             * Up to 40 hotels for recommendation.
             */
            int limit =
                    Math.min(
                            40,
                            propertiesNode.size()
                    );

            for (int i = 0; i < limit; i++) {

                JsonNode hotel =
                        propertiesNode.get(i);

                hotels.add(
                        parseHotel(hotel)
                );
            }

            return hotels;

        } catch (com.OdysseyAi.demo.common.exception.customexception.UnsupportedLocationException ex) {
            throw ex;
        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to parse SerpApi hotel response.",
                    ex
            );
        }
    }

    private HotelData parseHotel(
            JsonNode hotel
    ) {

        JsonNode coordinates =
                hotel.path("gps_coordinates");

        JsonNode nightlyRate =
                hotel.path("rate_per_night");

        JsonNode totalRate =
                hotel.path("total_rate");

        JsonNode images =
                hotel.path("images");

        List<String> imageUrls =
                new ArrayList<>();

        if (images.isArray()) {

            for (JsonNode image : images) {

                String thumbnail =
                        image.path("thumbnail")
                                .asText(null);

                if (thumbnail != null
                        && !thumbnail.isBlank()) {

                    imageUrls.add(thumbnail);
                }
            }
        }

        List<String> amenities =
                new ArrayList<>();

        JsonNode amenitiesNode =
                hotel.path("amenities");

        if (amenitiesNode.isArray()) {

            for (JsonNode amenity :
                    amenitiesNode) {

                amenities.add(
                        amenity.asText()
                );
            }
        }

        BigDecimal nightlyPrice =
                readDecimal(
                        nightlyRate,
                        "extracted_lowest"
                );

        BigDecimal totalPrice =
                readDecimal(
                        totalRate,
                        "extracted_lowest"
                );

        BigDecimal beforeTaxesNightlyPrice =
                readDecimal(
                        nightlyRate,
                        "extracted_before_taxes_fees"
                );

        return HotelData.builder()
                .propertyToken(
                        hotel.path("property_token")
                                .asText(null)
                )
                .name(
                        hotel.path("name")
                                .asText(null)
                )
                .description(
                        hotel.path("description")
                                .asText(null)
                )
                .address(
                        hotel.path("address")
                                .asText(null)
                )
                .latitude(
                        coordinates.has("latitude")
                                ? coordinates.path("latitude")
                                .asDouble()
                                : null
                )
                .longitude(
                        coordinates.has("longitude")
                                ? coordinates.path("longitude")
                                .asDouble()
                                : null
                )
                .rating(
                        hotel.has("overall_rating")
                                ? hotel.path("overall_rating")
                                .asDouble()
                                : null
                )
                .reviewCount(
                        hotel.has("reviews")
                                ? hotel.path("reviews")
                                .asInt()
                                : null
                )
                .hotelClass(
                        hotel.has("extracted_hotel_class")
                                ? hotel.path(
                                "extracted_hotel_class"
                        ).asInt()
                                : null
                )
                .nightlyPrice(
                        nightlyPrice
                )
                .totalPrice(
                        totalPrice
                )
                .beforeTaxesNightlyPrice(
                        beforeTaxesNightlyPrice
                )
                .currency(
                        null
                )
                .priceDisplay(
                        nightlyRate.path("lowest")
                                .asText(null)
                )
                .amenities(
                        amenities
                )
                .imageUrls(
                        imageUrls
                )
                .build();
    }

    private BigDecimal readDecimal(
            JsonNode node,
            String field
    ) {

        if (node == null
                || node.isMissingNode()
                || !node.has(field)) {

            return null;
        }

        JsonNode value =
                node.get(field);

        if (!value.isNumber()) {
            return null;
        }

        return value.decimalValue();
    }
}