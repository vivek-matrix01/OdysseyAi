package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.request.GenerateItineraryRequest;
import com.OdysseyAi.demo.travel.dto.HotelData;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.dto.RouteData;
import com.OdysseyAi.demo.travel.dto.TravelContext;
import com.OdysseyAi.demo.travel.dto.WeatherData;
import com.OdysseyAi.demo.trip.entity.Trip;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptBuilder {

    private static final int MAX_ATTRACTIONS = 20;
    private static final int MAX_RESTAURANTS = 15;
    private static final int MAX_ROUTES = 6;

    public String buildItineraryPrompt(
            Trip trip,
            GenerateItineraryRequest request,
            TravelContext travelContext
    ) {

        return """
                You are a travel itinerary planning assistant.

                Create a practical and personalized itinerary using
                the real travel data supplied below.

                TRIP
                Destination: %s
                Start Date: %s
                End Date: %s
                Description: %s

                USER PREFERENCES
                Preferences: %s
                Number Of Days: %d
                Special Requirements: %s
                Currency: %s

                REAL TRAVEL DATA

                %s

                RULES

                1. Use supplied places whenever possible.
                2. Do not invent places when suitable supplied places exist.
                3. Respect the supplied weather conditions.
                4. Prefer geographically practical combinations.
                5. Use supplied route durations/distances when relevant.
                6. Use supplied place pricing information when available.
                7. Day numbers must be sequential starting from 1.
                8. Dates must remain inside the Trip dates.
                9. Activity startTime must be before endTime.
                10. estimatedCost must not be negative.
                11. Use only these ActivityType values:
                    SIGHTSEEING, FOOD, HOTEL, TRANSPORT,
                    SHOPPING, ENTERTAINMENT, OTHER.
                12. Return only valid JSON.
                13. Do not return markdown.
                14. Do not return explanations outside the JSON.
                15. Do not invent IDs.
                16. Every day must contain a "notes" field.
                17. "notes" must ALWAYS be a JSON string.
                18. NEVER use null for "notes".
                19. If there are no notes, use an empty string "".
                20. Every activity must contain:
                    title, description, startTime, endTime,
                    location, estimatedCost, currency, type.
                21. Never use null for required activity fields.
                22. Use the exact supplied place names for locations.
                
                JSON REQUIREMENTS

                The response must follow this structure exactly:

                {
                  "days": [
                    {
                      "dayNumber": 1,
                      "date": "YYYY-MM-DD",
                      "notes": "string",
                      "activities": [
                        {
                          "title": "string",
                          "description": "string",
                          "startTime": "HH:mm:ss",
                          "endTime": "HH:mm:ss",
                          "location": "string",
                          "estimatedCost": 0.0,
                          "currency": "INR",
                          "type": "SIGHTSEEING"
                        }
                      ]
                    }
                  ]
                }

                IMPORTANT:
                - "notes" must never be null.
                - "estimatedCost" must always be a number.
                - "location" must always be a string.
                - Use exact names from REAL TRAVEL DATA.
                - Do not create generic names such as "Hotel" when
                  an exact hotel name is supplied.
                """
                .formatted(
                        trip.getDestination(),
                        trip.getStartDate(),
                        trip.getEndDate(),
                        nullToEmpty(
                                trip.getDescription()
                        ),
                        nullToEmpty(
                                request.getPreferences()
                        ),
                        request.getNumberOfDays(),
                        nullToEmpty(
                                request.getSpecialRequirements()
                        ),
                        request.getCurrency(),
                        formatTravelContext(
                                travelContext
                        )
                );
    }


    private String formatTravelContext(
            TravelContext context
    ) {

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                "ATTRACTIONS\n"
        );

        List<PlaceData> places =
                context.getPlaces();

        if (places == null) {
            places = List.of();
        }

        int attractionCount = 0;
        int restaurantCount = 0;

        for (PlaceData place :
                places) {

            if (place == null) {
                continue;
            }

            String type =
                    nullToEmpty(
                            place.getPrimaryType()
                    ).toLowerCase();

            boolean isRestaurant =
                    type.contains("restaurant")
                            || type.contains("cafe")
                            || type.contains("food");

            if (isRestaurant) {

                if (restaurantCount >=
                        MAX_RESTAURANTS) {

                    continue;
                }

                restaurantCount++;

                appendPlace(
                        builder,
                        place,
                        "RESTAURANT"
                );

            } else {

                if (attractionCount >=
                        MAX_ATTRACTIONS) {

                    continue;
                }

                attractionCount++;

                appendPlace(
                        builder,
                        place,
                        "ATTRACTION"
                );
            }

            if (attractionCount >= MAX_ATTRACTIONS
                    && restaurantCount >= MAX_RESTAURANTS) {

                break;
            }
        }


        /*
         * --------------------------------------------------------
         * WEATHER
         * --------------------------------------------------------
         */
        builder.append(
                "\nWEATHER\n"
        );

        if (context.getWeather() != null) {

            for (WeatherData weather :
                    context.getWeather()) {

                if (weather == null) {
                    continue;
                }

                builder.append(
                        "%s | min %.1f°C | max %.1f°C | rain %.1f mm | wind %.1f km/h | code %d%n"
                                .formatted(
                                        weather.getDate(),
                                        safeDouble(
                                                weather.getTemperatureMin()
                                        ),
                                        safeDouble(
                                                weather.getTemperatureMax()
                                        ),
                                        safeDouble(
                                                weather.getPrecipitation()
                                        ),
                                        safeDouble(
                                                weather.getWindSpeed()
                                        ),
                                        weather.getWeatherCode()
                                )
                );
            }
        }


        /*
         * --------------------------------------------------------
         * ROUTES
         * --------------------------------------------------------
         */
        builder.append(
                "\nROUTES\n"
        );

        int routeCount = 0;

        if (context.getRoutes() != null) {

            for (RouteData route :
                    context.getRoutes()) {

                if (route == null) {
                    continue;
                }

                if (routeCount >= MAX_ROUTES) {
                    break;
                }

                double distanceKm =
                        route.getDistanceMeters() == null
                                ? 0.0
                                : route.getDistanceMeters()
                                  / 1000.0;

                double durationMinutes =
                        route.getDurationSeconds() == null
                                ? 0.0
                                : route.getDurationSeconds()
                                  / 60.0;

                builder.append(
                        "%s -> %s | mode=%s | %.1f km | %.0f min"
                                .formatted(
                                        nullToEmpty(
                                                route.getOrigin()
                                        ),
                                        nullToEmpty(
                                                route.getDestination()
                                        ),
                                        nullToEmpty(
                                                route.getTravelMode()
                                        ),
                                        distanceKm,
                                        durationMinutes
                                )
                );

                if (route.getFare() != null) {

                    builder.append(
                            " | fare=%s %s"
                                    .formatted(
                                            route.getFare(),
                                            nullToEmpty(
                                                    route.getFareCurrency()
                                            )
                                    )
                    );
                }

                builder.append("\n");

                routeCount++;
            }
        }


        /*
         * --------------------------------------------------------
         * CURRENCY
         * --------------------------------------------------------
         */
        builder.append(
                "\nCURRENCY\n"
        );

        builder.append(
                "%s -> %s | rate=%s%n"
                        .formatted(
                                nullToEmpty(
                                        context.getBaseCurrency()
                                ),
                                nullToEmpty(
                                        context.getTargetCurrency()
                                ),
                                context.getExchangeRate()
                        )
        );

        return builder.toString();
    }


    private void appendPlace(
            StringBuilder builder,
            PlaceData place,
            String category
    ) {

        builder.append(
                "%s | id=%s | %s | rating=%s | reviewCount=%s | priceLevel=%s | range=%s-%s | latitude=%s | longitude=%s%n"
                        .formatted(
                                category,
                                nullToEmpty(
                                        place.getId()
                                ),
                                nullToEmpty(
                                        place.getName()
                                ),
                                place.getRating(),
                                place.getUserRatingCount(),
                                nullToEmpty(
                                        place.getPriceLevel()
                                ),
                                place.getMinPrice(),
                                place.getMaxPrice(),
                                place.getLatitude(),
                                place.getLongitude()
                        )
        );
    }


    private double safeDouble(
            Double value
    ) {

        return value == null
                ? 0.0
                : value;
    }


    private String nullToEmpty(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}
