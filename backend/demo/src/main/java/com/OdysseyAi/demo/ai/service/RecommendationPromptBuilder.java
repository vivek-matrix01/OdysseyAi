package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.request.TripRecommendationRequest;
import com.OdysseyAi.demo.travel.dto.HotelData;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.dto.RouteData;
import com.OdysseyAi.demo.travel.dto.TravelContext;
import com.OdysseyAi.demo.travel.dto.WeatherData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecommendationPromptBuilder {

    public String buildPrompt(
            TripRecommendationRequest request,
            TravelContext context,
            List<HotelData> hotels
    ) {

        return """
                You are OdysseyAI, a travel recommendation engine.

                Your task is to recommend the best travel choices
                using the REAL DATA supplied below.

                USER REQUEST

                Trip type:
                %s

                Destination:
                %s

                Budget:
                %s %s

                Travelers:
                %d

                Start date:
                %s

                End date:
                %s

                Preferences:
                %s

                REAL TRAVEL DATA

                %s

                HOTEL DATA

                %s

                RECOMMENDATION RULES

                1. Match recommendations to the user's trip type.
                2. Respect the user's budget.
                3. Prefer highly rated options when appropriate.
                4. Focus on places and attractions that best match the destination experience, user interests, and ratings. Do NOT restrict or filter out places based on distance.
                5. Provide a diverse mix of culture, nature, dining, and sights regardless of distance. Route distance will only be calculated after specific places are chosen by the user or AI.
                6. Consider weather when ranking outdoor activities.
                7. Use real hotel prices when hotel data is supplied.
                8. Never invent hotels.
                9. Never invent hotel prices.
                10. Never invent places that are not supplied.
                11. Use the exact placeId supplied for a recommended place.
                12. Do not modify or invent a placeId.
                13. Use the exact propertyToken supplied for a recommended hotel.
                14. Do not modify or invent a propertyToken.
                15. If hotel data is unavailable, return an empty hotels array.
                16. Do not return markdown.
                17. Return only valid JSON.
                18. Provide a rich and generous selection of options: recommend at least 12 to 16 places/attractions (spanning sightseeing, culture, nature, and food) and at least 8 to 12 verified hotels from the supplied data so the user has plenty of diverse options to pick from.
                19. Risk & Problem Prediction: Analyze the destination weather, seasonality, distances between places, timing/closings, and budget. Generate a "potentialProblems" array containing 3 to 5 realistic, insightful warning points predicting what things could cause problems during this trip (e.g. weather/rain disruptions, heavy traffic between far attractions, peak-season surge prices, closing days for monuments/museums, or physical terrain challenges) with practical advice on how to avoid them.

                RETURN EXACTLY THIS JSON STRUCTURE:

                {
                  "destination": "string",
                  "tripType": "string",
                  "budget": 0.0,
                  "currency": "INR",
                  "travelers": 1,
                  "estimatedTotalCost": 0.0,
                  "places": [
                    {
                      "placeId": "string",
                      "name": "string",
                      "address": "string",
                      "category": "string",
                      "rating": 0.0,
                      "userRatingCount": 0,
                      "priceLevel": "string",
                      "estimatedCost": 0.0,
                      "reason": "string"
                    }
                  ],
                  "hotels": [
                    {
                      "propertyToken": "string",
                      "name": "string",
                      "address": "string",
                      "rating": 0.0,
                      "priceLevel": "string",
                      "estimatedNightlyCost": 0.0,
                      "reason": "string"
                    }
                  ],
                  "recommendationReason": "string",
                  "potentialProblems": [
                    "string"
                  ]
                }

                Only recommend places from the supplied PLACES data.
                Only recommend hotels from the supplied HOTEL DATA.
                """.formatted(
                request.getTripType(),
                request.getDestination(),
                request.getBudget(),
                request.getCurrency(),
                request.getTravelers(),
                request.getStartDate(),
                request.getEndDate(),
                nullToEmpty(request.getPreferences()),
                formatTravelContext(context),
                formatHotels(hotels)
        );
    }

    private String formatTravelContext(
            TravelContext context
    ) {

        StringBuilder builder =
                new StringBuilder();

        builder.append("PLACES\n");

        int placeCount = 0;

        for (PlaceData place :
                context.getPlaces()) {

            if (placeCount >= 35) {
                break;
            }

            builder.append(
                    "- placeId=%s | name=%s | address=%s | type=%s | rating=%s | reviews=%s | priceLevel=%s | range=%s-%s%n"
                            .formatted(
                                    nullToEmpty(place.getId()),
                                    nullToEmpty(place.getName()),
                                    nullToEmpty(place.getAddress()),
                                    nullToEmpty(place.getPrimaryType()),
                                    place.getRating(),
                                    place.getUserRatingCount(),
                                    nullToEmpty(place.getPriceLevel()),
                                    place.getMinPrice(),
                                    place.getMaxPrice()
                            )
            );

            placeCount++;
        }

        builder.append("\nWEATHER\n");

        if (context.getWeather() == null
                || context.getWeather().isEmpty()) {

            builder.append(
                    "Weather forecast unavailable.\n"
            );

        } else {

            for (WeatherData weather :
                    context.getWeather()) {

                builder.append(
                        "- %s | %s | rain=%s | probability=%.0f%% | min=%.1f°C | max=%.1f°C%n"
                                .formatted(
                                        weather.getDate(),
                                        nullToEmpty(weather.getCondition()),
                                        weather.getRainExpected(),
                                        safe(weather.getPrecipitationProbability()),
                                        safe(weather.getTemperatureMin()),
                                        safe(weather.getTemperatureMax())
                                )
                );
            }
        }

        builder.append("\nROUTES\n");

        if (context.getRoutes() != null) {

            int routeCount = 0;

            for (RouteData route :
                    context.getRoutes()) {

                if (routeCount >= 6) {
                    break;
                }

                double distanceKm =
                        route.getDistanceMeters() == null
                                ? 0
                                : route.getDistanceMeters() / 1000.0;

                double durationMinutes =
                        route.getDurationSeconds() == null
                                ? 0
                                : route.getDurationSeconds() / 60.0;

                builder.append(
                        "- %s -> %s | mode=%s | %.1fkm | %.0fmin | fare=%s %s%n"
                                .formatted(
                                        nullToEmpty(route.getOrigin()),
                                        nullToEmpty(route.getDestination()),
                                        nullToEmpty(route.getTravelMode()),
                                        distanceKm,
                                        durationMinutes,
                                        route.getFare(),
                                        nullToEmpty(route.getFareCurrency())
                                )
                );

                routeCount++;
            }
        }

        builder.append("\nCURRENCY\n");

        builder.append(
                "- base=%s | target=%s | rate=%s%n"
                        .formatted(
                                nullToEmpty(context.getBaseCurrency()),
                                nullToEmpty(context.getTargetCurrency()),
                                context.getExchangeRate()
                        )
        );

        return builder.toString();
    }

    private String formatHotels(
            List<HotelData> hotels
    ) {

        if (hotels == null
                || hotels.isEmpty()) {

            return "No hotel data available.";
        }

        StringBuilder builder =
                new StringBuilder();

        int limit =
                Math.min(25, hotels.size());

        for (int i = 0; i < limit; i++) {

            HotelData hotel =
                    hotels.get(i);

            builder.append(
                    "- propertyToken=%s | name=%s | address=%s | rating=%s | reviews=%s | class=%s | nightly=%s | total=%s | price=%s%n"
                            .formatted(
                                    nullToEmpty(hotel.getPropertyToken()),
                                    nullToEmpty(hotel.getName()),
                                    nullToEmpty(hotel.getAddress()),
                                    hotel.getRating(),
                                    hotel.getReviewCount(),
                                    hotel.getHotelClass(),
                                    hotel.getNightlyPrice(),
                                    hotel.getTotalPrice(),
                                    nullToEmpty(hotel.getPriceDisplay())
                            )
            );
        }

        return builder.toString();
    }

    private double safe(
            Double value
    ) {

        return value == null ? 0.0 : value;
    }

    private String nullToEmpty(
            String value
    ) {

        return value == null ? "" : value;
    }
}