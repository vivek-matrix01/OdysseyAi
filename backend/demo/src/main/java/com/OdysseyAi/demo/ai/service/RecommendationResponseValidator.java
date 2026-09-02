package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.response.RecommendedHotelResponse;
import com.OdysseyAi.demo.ai.dto.response.RecommendedPlaceResponse;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.travel.dto.HotelData;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RecommendationResponseValidator {

    public void validate(
            TripRecommendationResponse response,
            List<PlaceData> availablePlaces,
            List<HotelData> availableHotels
    ) {

        if (response == null) {

            throw new BadRequestException(
                    "AI returned an empty recommendation response."
            );
        }

        validatePlaces(
                response.getPlaces(),
                availablePlaces
        );

        validateHotels(
                response.getHotels(),
                availableHotels
        );
    }


    /*
     * ============================================================
     * PLACE VALIDATION
     * ============================================================
     *
     * The AI is instructed to return the exact Google placeId.
     *
     * However, LLMs can occasionally:
     *
     * - copy the wrong ID
     * - truncate an ID
     * - slightly modify an ID
     *
     * When that happens, we try to recover using the exact place
     * name supplied by Google.
     *
     * We NEVER invent a new place.
     *
     * We only replace an invalid AI placeId with an ID belonging
     * to a place that Google actually returned.
     */
    private void validatePlaces(
            List<RecommendedPlaceResponse> recommendedPlaces,
            List<PlaceData> availablePlaces
    ) {

        if (recommendedPlaces == null) {
            return;
        }

        if (availablePlaces == null) {

            throw new BadRequestException(
                    "Google Places data is unavailable."
            );
        }


        /*
         * --------------------------------------------------------
         * Build lookup by Google place ID.
         * --------------------------------------------------------
         */
        Set<String> availableIds =
                new HashSet<>();


        /*
         * --------------------------------------------------------
         * Build lookup by normalized place name.
         *
         * Example:
         *
         * "Shri Kamakshi Temple"
         *
         * becomes:
         *
         * "shri kamakshi temple"
         * --------------------------------------------------------
         */
        Map<String, PlaceData> placesByName =
                new HashMap<>();


        for (PlaceData place : availablePlaces) {

            if (place == null) {
                continue;
            }


            String id =
                    clean(place.getId());

            if (!id.isBlank()) {

                availableIds.add(id);
            }


            String name =
                    normalizeName(
                            place.getName()
                    );

            if (!name.isBlank()) {

                placesByName.putIfAbsent(
                        name,
                        place
                );
            }
        }


        /*
         * --------------------------------------------------------
         * Track duplicates after repairing IDs.
         * --------------------------------------------------------
         */
        Set<String> selectedIds =
                new HashSet<>();


        for (
                RecommendedPlaceResponse recommendedPlace
                : recommendedPlaces
        ) {

            if (recommendedPlace == null) {

                throw new BadRequestException(
                        "AI returned a null place recommendation."
                );
            }


            String aiPlaceId =
                    clean(
                            recommendedPlace.getPlaceId()
                    );


            String placeName =
                    clean(
                            recommendedPlace.getName()
                    );


            /*
             * ----------------------------------------------------
             * Case 1:
             *
             * AI returned a valid Google place ID.
             * ----------------------------------------------------
             */
            if (!aiPlaceId.isBlank()
                    && availableIds.contains(aiPlaceId)) {

                if (!selectedIds.add(aiPlaceId)) {

                    throw new BadRequestException(
                            "AI returned the same place more than once."
                    );
                }

                continue;
            }


            /*
             * ----------------------------------------------------
             * Case 2:
             *
             * AI returned an invalid ID, but the name matches
             * a real Google place.
             *
             * Repair the ID using Google's actual ID.
             * ----------------------------------------------------
             */
            PlaceData matchedPlace =
                    findMatchingPlaceByName(
                            placeName,
                            placesByName
                    );


            if (matchedPlace != null) {

                String realPlaceId =
                        clean(
                                matchedPlace.getId()
                        );


                if (realPlaceId.isBlank()) {

                    throw new BadRequestException(
                            "Google returned a place without a valid placeId."
                    );
                }


                if (!selectedIds.add(realPlaceId)) {

                    throw new BadRequestException(
                            "AI returned the same place more than once."
                    );
                }


                /*
                 * IMPORTANT:
                 *
                 * Replace the AI-generated ID with the real
                 * Google Places ID.
                 */
                recommendedPlace.setPlaceId(
                        realPlaceId
                );


                /*
                 * Also normalize the name to the authoritative
                 * Google name when available.
                 */
                if (!clean(matchedPlace.getName()).isBlank()) {

                    recommendedPlace.setName(
                            matchedPlace.getName()
                    );
                }


                continue;
            }


            /*
             * ----------------------------------------------------
             * Case 3:
             *
             * Neither ID nor name can be matched.
             *
             * This is a genuinely invented place.
             * Reject it.
             * ----------------------------------------------------
             */
            if (aiPlaceId.isBlank()) {

                throw new BadRequestException(
                        "AI returned a place without a placeId and the place could not be matched by name."
                );
            }


            throw new BadRequestException(
                    "AI selected a place that was not returned by Google Places."
            );
        }
    }


    /*
     * ============================================================
     * HOTEL VALIDATION
     * ============================================================
     *
     * Hotel propertyToken is provider-specific and should not be
     * reconstructed from the hotel name.
     *
     * Therefore hotel validation remains strict.
     * ============================================================
     */
    private void validateHotels(
            List<RecommendedHotelResponse> recommendedHotels,
            List<HotelData> availableHotels
    ) {

        if (recommendedHotels == null) {
            return;
        }

        if (availableHotels == null) {

            throw new BadRequestException(
                    "Hotel provider data is unavailable."
            );
        }


        Set<String> availableTokens =
                new HashSet<>();


        for (HotelData hotel : availableHotels) {

            if (hotel == null) {
                continue;
            }


            String propertyToken =
                    clean(
                            hotel.getPropertyToken()
                    );


            if (!propertyToken.isBlank()) {

                availableTokens.add(
                        propertyToken
                );
            }
        }


        Set<String> selectedTokens =
                new HashSet<>();


        for (
                RecommendedHotelResponse hotel
                : recommendedHotels
        ) {

            if (hotel == null) {

                throw new BadRequestException(
                        "AI returned a null hotel recommendation."
                );
            }


            String propertyToken =
                    clean(
                            hotel.getPropertyToken()
                    );


            if (propertyToken.isBlank()) {

                throw new BadRequestException(
                        "AI returned a hotel without a propertyToken."
                );
            }


            if (!availableTokens.contains(
                    propertyToken
            )) {

                throw new BadRequestException(
                        "AI selected a hotel that was not returned by the hotel provider."
                );
            }


            if (!selectedTokens.add(
                    propertyToken
            )) {

                throw new BadRequestException(
                        "AI returned the same hotel more than once."
                );
            }
        }
    }


    /*
     * ============================================================
     * PLACE NAME MATCHING
     * ============================================================
     */
    private PlaceData findMatchingPlaceByName(
            String aiName,
            Map<String, PlaceData> placesByName
    ) {

        String normalizedAiName =
                normalizeName(aiName);


        if (normalizedAiName.isBlank()) {
            return null;
        }


        /*
         * First try exact normalized name.
         */
        PlaceData exactMatch =
                placesByName.get(
                        normalizedAiName
                );


        if (exactMatch != null) {
            return exactMatch;
        }


        /*
         * Then allow a conservative contains match.
         *
         * Example:
         *
         * AI:
         * "Shri Kamakshi Temple, Goa"
         *
         * Google:
         * "Shri Kamakshi Temple"
         */
        for (
                Map.Entry<String, PlaceData> entry
                : placesByName.entrySet()
        ) {

            String availableName =
                    entry.getKey();


            if (
                    availableName.contains(
                            normalizedAiName
                    )
                            || normalizedAiName.contains(
                            availableName
                    )
            ) {

                return entry.getValue();
            }
        }


        return null;
    }


    /*
     * ============================================================
     * NORMALIZE NAME
     * ============================================================
     */
    private String normalizeName(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase()
                .replaceAll(
                        "\\s+",
                        " "
                )
                .replaceAll(
                        "[,.;:]+$",
                        ""
                );
    }


    /*
     * ============================================================
     * CLEAN STRING
     * ============================================================
     */
    private String clean(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }
}
