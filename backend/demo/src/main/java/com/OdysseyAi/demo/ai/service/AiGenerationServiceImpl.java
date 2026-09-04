package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.request.GenerateItineraryRequest;
import com.OdysseyAi.demo.ai.dto.request.GenerateSelectedItineraryRequest;
import com.OdysseyAi.demo.ai.dto.response.GeneratedItineraryResponse;
import com.OdysseyAi.demo.ai.interfaces.AiClient;
import com.OdysseyAi.demo.ai.interfaces.AiGenerationService;
import com.OdysseyAi.demo.ai.mapper.AiItineraryMapper;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.interfaces.AuthenticationFacade;

import com.OdysseyAi.demo.common.exception.customexception.AiGenerationException;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.common.exception.customexception.ResourceNotFoundException;

import com.OdysseyAi.demo.travel.dto.HotelData;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.dto.RouteData;
import com.OdysseyAi.demo.travel.dto.TravelContext;

import com.OdysseyAi.demo.travel.interfaces.HotelClient;
import com.OdysseyAi.demo.travel.interfaces.PlacesClient;
import com.OdysseyAi.demo.travel.interfaces.TravelDataService;

import com.OdysseyAi.demo.travel.service.HotelLocationResolver;
import com.OdysseyAi.demo.travel.service.ItineraryRouteService;
import com.OdysseyAi.demo.travel.service.TransportCostService;
import com.OdysseyAi.demo.travel.service.TripCostService;

import com.OdysseyAi.demo.trip.dto.response.ItineraryResponse;
import com.OdysseyAi.demo.trip.entity.Activity;
import com.OdysseyAi.demo.trip.entity.Itinerary;
import com.OdysseyAi.demo.trip.entity.ItineraryDay;
import com.OdysseyAi.demo.trip.entity.Trip;
import com.OdysseyAi.demo.trip.interfaces.TripAccessService;
import com.OdysseyAi.demo.trip.mapper.ItineraryMapper;
import com.OdysseyAi.demo.trip.repository.ItineraryRepository;
import com.OdysseyAi.demo.trip.repository.TripRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiGenerationServiceImpl
        implements AiGenerationService {

    private static final int MAX_SELECTED_ITINERARY_ATTEMPTS = 2;

    private final TripRepository tripRepository;
    private final ItineraryRepository itineraryRepository;
    private final HotelLocationResolver hotelLocationResolver;

    private final AuthenticationFacade authenticationFacade;
    private final TripAccessService tripAccessService;

    private final PromptBuilder promptBuilder;
    private final TravelDataService travelDataService;

    private final PlacesClient placesClient;
    private final HotelClient hotelClient;

    private final AiClient aiClient;

    private final GeneratedItineraryValidator generatedItineraryValidator;
    private final SelectedItineraryValidator selectedItineraryValidator;

    private final AiItineraryMapper aiItineraryMapper;
    private final ItineraryMapper itineraryMapper;

    private final TransportCostService transportCostService;
    private final ItineraryRouteService itineraryRouteService;
    private final TripCostService tripCostService;


    /*
     * ============================================================
     * NORMAL AI ITINERARY GENERATION
     * ============================================================
     */
    @Override
    @Transactional
    public ItineraryResponse generateItinerary(
            Long tripId,
            GenerateItineraryRequest request
    ) {

        User currentUser =
                authenticationFacade.getCurrentUser();

        Trip trip =
                tripRepository.findById(tripId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Trip not found."
                                )
                        );

        tripAccessService.requireEditor(
                trip,
                currentUser
        );

        validateRequest(
                request,
                trip
        );

        if (itineraryRepository.existsByTrip(trip)) {

            throw new BadRequestException(
                    "An itinerary already exists for this trip."
            );
        }

        TravelContext travelContext;

        try {

            travelContext =
                    travelDataService.buildTravelContext(
                            trip,
                            request.getCurrency()
                    );

        } catch (com.OdysseyAi.demo.common.exception.customexception.UnsupportedLocationException ex) {

            throw ex;

        } catch (AiGenerationException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new AiGenerationException(
                    "Failed to retrieve travel data required for itinerary generation.",
                    ex
            );
        }

        String prompt =
                promptBuilder.buildItineraryPrompt(
                        trip,
                        request,
                        travelContext
                );

        GeneratedItineraryResponse generatedResponse =
                aiClient.generateItinerary(
                        prompt
                );

        generatedItineraryValidator.validate(
                generatedResponse,
                trip,
                request.getNumberOfDays()
        );

        Itinerary itinerary =
                aiItineraryMapper.toEntity(
                        generatedResponse
                );

        itinerary.assignTrip(trip);
        itinerary.markGenerated();

        Itinerary savedItinerary =
                itineraryRepository.save(
                        itinerary
                );

        return itineraryMapper.toResponse(
                savedItinerary
        );
    }


    /*
     * ============================================================
     * SELECTED RECOMMENDATIONS -> FINAL AI ITINERARY
     * ============================================================
     */

    @Override
    @Transactional
    public ItineraryResponse generateSelectedItinerary(
            GenerateSelectedItineraryRequest request
    ) {

        /*
         * --------------------------------------------------------
         * 1. Validate request
         * --------------------------------------------------------
         */
        validateSelectedRequest(request);


        /*
         * --------------------------------------------------------
         * 2. Current user
         * --------------------------------------------------------
         */
        User currentUser =
                authenticationFacade.getCurrentUser();


        /*
         * --------------------------------------------------------
         * 3. Find trip
         * --------------------------------------------------------
         */
        Trip trip =
                tripRepository.findById(request.getTripId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Trip not found."
                                )
                        );


        /*
         * --------------------------------------------------------
         * 4. User must have edit access
         * --------------------------------------------------------
         */
        tripAccessService.requireEditor(
                trip,
                currentUser
        );


        /*
         * --------------------------------------------------------
         * 5. Do not allow duplicate itinerary
         * --------------------------------------------------------
         */
        if (itineraryRepository.existsByTrip(trip)) {

            throw new BadRequestException(
                    "An itinerary already exists for this trip."
            );
        }


        /*
         * --------------------------------------------------------
         * 6. Fetch travel data WITHOUT candidate routes
         *
         * We deliberately avoid building recommendation routes
         * here because actual routes will be calculated later
         * using the locations selected by the user.
         * --------------------------------------------------------
         */
        TravelContext originalContext;

        try {

            originalContext =
                    travelDataService
                            .buildTravelContextWithoutRoutes(
                                    trip,
                                    request.getCurrency()
                            );

        } catch (com.OdysseyAi.demo.common.exception.customexception.UnsupportedLocationException ex) {

            throw ex;

        } catch (AiGenerationException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new AiGenerationException(
                    "Failed to retrieve travel data required for selected itinerary generation.",
                    ex
            );
        }


        /*
         * --------------------------------------------------------
         * 7. Resolve selected places
         * --------------------------------------------------------
         */
        List<PlaceData> selectedPlaces =
                findSelectedPlaces(
                        request.getSelectedPlaceIds()
                );


        /*
         * --------------------------------------------------------
         * 8. Resolve selected hotel
         *
         * The selected hotel is already supplied by the client,
         * so we do NOT perform another hotel search.
         *
         * This prevents property-token mismatch caused by a fresh
         * hotel search returning different results.
         * --------------------------------------------------------
         */
        HotelData selectedHotel =
                findSelectedHotel(
                        request.getHotel()
                );


        /*
         * --------------------------------------------------------
         * 9. Resolve hotel coordinates
         *
         * Recommendation responses may not contain latitude /
         * longitude for the selected hotel.
         *
         * Route calculation requires coordinates, so resolve them
         * before calculating itinerary routes.
         *
         * If resolution fails, the resolver simply returns the
         * existing hotel object and the itinerary can still continue.
         * --------------------------------------------------------
         */
        if (selectedHotel != null) {

            selectedHotel =
                    hotelLocationResolver.resolveCoordinates(
                            selectedHotel
                    );
        }


        /*
         * --------------------------------------------------------
         * 10. Build reduced travel context
         *
         * Only the user's selected places are exposed to AI.
         * Candidate routes are intentionally empty.
         * --------------------------------------------------------
         */
        TravelContext selectedContext =
                TravelContext.builder()
                        .destination(
                                originalContext.getDestination()
                        )
                        .places(
                                selectedPlaces
                        )
                        .weather(
                                originalContext.getWeather()
                        )
                        .routes(
                                new ArrayList<>()
                        )
                        .baseCurrency(
                                originalContext.getBaseCurrency()
                        )
                        .targetCurrency(
                                originalContext.getTargetCurrency()
                        )
                        .exchangeRate(
                                originalContext.getExchangeRate()
                        )
                        .build();


        /*
         * --------------------------------------------------------
         * 11. Calculate trip duration
         *
         * Inclusive of start and end date.
         * Example:
         *
         * Sep 5 -> Sep 8 = 4 days
         * --------------------------------------------------------
         */
        long tripDuration =
                ChronoUnit.DAYS.between(
                        trip.getStartDate(),
                        trip.getEndDate()
                ) + 1;


        /*
         * --------------------------------------------------------
         * 12. Create internal itinerary request
         * --------------------------------------------------------
         */
        GenerateItineraryRequest itineraryRequest =
                new GenerateItineraryRequest();

        itineraryRequest.setNumberOfDays(
                Math.toIntExact(
                        tripDuration
                )
        );

        itineraryRequest.setCurrency(
                request.getCurrency()
        );


        /*
         * --------------------------------------------------------
         * 13. Build base AI prompt
         * --------------------------------------------------------
         */
        String basePrompt =
                promptBuilder.buildItineraryPrompt(
                        trip,
                        itineraryRequest,
                        selectedContext
                );


        /*
         * --------------------------------------------------------
         * 14. Add exact selected hotel / places to prompt
         * --------------------------------------------------------
         */
        String selectionContext =
                buildSelectionContext(
                        selectedHotel,
                        selectedPlaces,
                        request.getAdditionalPreferences()
                );


        /*
         * --------------------------------------------------------
         * 15. Generate AI itinerary
         *
         * Maximum two attempts:
         *
         * Attempt 1 -> normal generation
         * Attempt 2 -> correction prompt if validation fails
         * --------------------------------------------------------
         */
        String lastValidationError = null;

        for (
                int attempt = 1;
                attempt <= MAX_SELECTED_ITINERARY_ATTEMPTS;
                attempt++
        ) {

            String prompt =
                    basePrompt
                            + selectionContext;


            /*
             * ----------------------------------------------------
             * Add correction instructions during retry.
             * ----------------------------------------------------
             */
            if (lastValidationError != null) {

                prompt =
                        prompt
                                + "\n\n"
                                + "PREVIOUS ATTEMPT FAILED.\n"
                                + "Reason: "
                                + lastValidationError
                                + "\n\n"
                                + "Generate the complete itinerary again.\n"
                                + "Use only the confirmed hotel and confirmed places.\n"
                                + "Do not introduce any new location.\n"
                                + "Every required JSON field must have the correct type.\n"
                                + "Do not use null for notes.\n"
                                + "Use an empty string for notes when necessary.\n";
            }


            /*
             * ----------------------------------------------------
             * 16. Call AI
             * ----------------------------------------------------
             */
            GeneratedItineraryResponse generatedResponse;

            try {

                generatedResponse =
                        aiClient.generateItinerary(
                                prompt
                        );

            } catch (AiGenerationException ex) {

                /*
                 * Groq can fail before our own validators execute.
                 * Therefore give it one retry.
                 */
                lastValidationError =
                        ex.getMessage();

                if (
                        attempt ==
                                MAX_SELECTED_ITINERARY_ATTEMPTS
                ) {

                    throw ex;
                }

                continue;
            }


            /*
             * ----------------------------------------------------
             * 17. Validate AI output
             * ----------------------------------------------------
             */
            try {

                generatedItineraryValidator.validate(
                        generatedResponse,
                        trip,
                        Math.toIntExact(
                                tripDuration
                        )
                );

                selectedItineraryValidator.validate(
                        generatedResponse,
                        selectedPlaces,
                        selectedHotel
                );


                /*
                 * ------------------------------------------------
                 * 18. Convert AI response to entity
                 * ------------------------------------------------
                 */
                Itinerary itinerary =
                        aiItineraryMapper.toEntity(
                                generatedResponse
                        );

                itinerary.assignTrip(trip);
                itinerary.markGenerated();

                /*
                 * Enrich activities with realistic costs if AI returned zero or null
                 */
                for (ItineraryDay d : itinerary.getDays()) {
                    if (d.getActivities() != null) {
                        for (Activity act : d.getActivities()) {
                            if (act.getEstimatedCost() == null || act.getEstimatedCost().compareTo(BigDecimal.ZERO) <= 0) {
                                BigDecimal fee = BigDecimal.valueOf(act.getType() == com.OdysseyAi.demo.common.enums.ActivityType.FOOD ? 300 : 200);
                                act.setEstimatedCost(fee);
                            }
                        }
                    }
                }

                /*
                 * ------------------------------------------------
                 * 19. Extract the actual itinerary locations
                 *
                 * These are the locations actually used by AI in
                 * the generated itinerary.
                 * ------------------------------------------------
                 */
                List<String> itineraryLocations =
                        extractItineraryLocations(
                                itinerary
                        );


                /*
                 * ------------------------------------------------
                 * 20. Calculate real routes
                 *
                 * Hotel -> first location
                 * Place -> place
                 * etc.
                 * ------------------------------------------------
                 */
                List<RouteData> routeData =
                        itineraryRouteService.calculateRoutes(
                                itineraryLocations,
                                selectedPlaces,
                                selectedHotel
                        );


                /*
                 * ------------------------------------------------
                 * 21. Calculate transport cost at trip creation time
                 * ------------------------------------------------
                 */
                BigDecimal estimatedTransportCost =
                        transportCostService
                                .calculateTransportCost(
                                        routeData
                                );

                if (estimatedTransportCost == null || estimatedTransportCost.compareTo(BigDecimal.ZERO) <= 0) {
                    if (itineraryLocations != null && itineraryLocations.size() >= 2) {
                        double estimatedKm = (itineraryLocations.size() - 1) * 14.5;
                        estimatedTransportCost = BigDecimal.valueOf(Math.round(estimatedKm * 14.0));
                    }
                }


                /*
                 * ------------------------------------------------
                 * 22. Calculate complete trip cost
                 *
                 * Hotel
                 * + Activities
                 * + Transport
                 * = Total
                 * ------------------------------------------------
                 */
                TripCostService.CostBreakdown costBreakdown =
                        tripCostService.calculate(
                                itinerary,
                                selectedHotel,
                                estimatedTransportCost
                        );


                /*
                 * ------------------------------------------------
                 * 23. Persist routes
                 * ------------------------------------------------
                 */
                itinerary.updateRoutes(
                        routeData
                );


                /*
                 * ------------------------------------------------
                 * 24. Persist cost breakdown
                 * ------------------------------------------------
                 */
                itinerary.updateCostBreakdown(
                        costBreakdown.hotelCost(),
                        costBreakdown.activityCost(),
                        costBreakdown.transportCost(),
                        costBreakdown.totalCost()
                );


                /*
                 * ------------------------------------------------
                 * 25. Save itinerary
                 * ------------------------------------------------
                 */
                Itinerary savedItinerary =
                        itineraryRepository.save(
                                itinerary
                        );


                /*
                 * ------------------------------------------------
                 * 26. Return persisted entity
                 * ------------------------------------------------
                 */
                return itineraryMapper.toResponse(
                        savedItinerary
                );

            } catch (BadRequestException ex) {

                lastValidationError =
                        ex.getMessage();

                /*
                 * Retry only validation failures.
                 */
                if (
                        attempt ==
                                MAX_SELECTED_ITINERARY_ATTEMPTS
                ) {

                    throw new BadRequestException(
                            "AI could not generate a valid itinerary "
                                    + "using only the selected locations after "
                                    + MAX_SELECTED_ITINERARY_ATTEMPTS
                                    + " attempts. Last validation error: "
                                    + lastValidationError
                    );
                }
            }
        }


        /*
         * --------------------------------------------------------
         * Should never be reached.
         * --------------------------------------------------------
         */
        throw new AiGenerationException(
                "Failed to generate selected itinerary."
        );
    }




    /*
     * ============================================================
     * SELECTED PLACE RESOLUTION
     * ============================================================
     */
    private List<PlaceData> findSelectedPlaces(
            List<String> selectedPlaceIds
    ) {

        if (selectedPlaceIds == null
                || selectedPlaceIds.isEmpty()) {

            return new ArrayList<>();
        }

        List<String> cleanedIds =
                selectedPlaceIds.stream()
                        .filter(id ->
                                id != null
                                        && !id.isBlank()
                        )
                        .map(String::trim)
                        .distinct()
                        .toList();

        if (cleanedIds.isEmpty()) {

            return new ArrayList<>();
        }

        List<PlaceData> selectedPlaces =
                new ArrayList<>();

        for (String placeId :
                cleanedIds) {

            try {

                PlaceData place =
                        placesClient.getPlaceById(
                                placeId
                        );

                if (place == null) {

                    throw new BadRequestException(
                            "Selected place could not be resolved: "
                                    + placeId
                    );
                }

                selectedPlaces.add(
                        place
                );

            } catch (BadRequestException ex) {

                throw ex;

            } catch (Exception ex) {

                throw new BadRequestException(
                        "Selected place could not be resolved: "
                                + placeId
                );
            }
        }

        return selectedPlaces;
    }


    /*
     * ============================================================
     * SELECTED HOTEL RESOLUTION
     * ============================================================
     */
    private HotelData findSelectedHotel(
            GenerateSelectedItineraryRequest.SelectedHotelRequest request
    ) {

        if (request == null) {
            return null;
        }

        return HotelData.builder()
                .propertyToken(
                        request.getPropertyToken()
                )
                .name(
                        request.getName()
                )
                .address(
                        request.getAddress()
                )
                .latitude(
                        request.getLatitude()
                )
                .longitude(
                        request.getLongitude()
                )
                .rating(
                        request.getRating()
                )
                .reviewCount(
                        request.getReviewCount()
                )
                .hotelClass(
                        request.getHotelClass()
                )
                .nightlyPrice(
                        request.getNightlyPrice()
                )
                .totalPrice(
                        request.getTotalPrice()
                )
                .beforeTaxesNightlyPrice(
                        request.getBeforeTaxesNightlyPrice()
                )
                .currency(
                        request.getCurrency()
                )
                .priceDisplay(
                        request.getPriceDisplay()
                )
                .amenities(
                        request.getAmenities() == null
                                ? new ArrayList<>()
                                : new ArrayList<>(
                                request.getAmenities()
                        )
                )
                .imageUrls(
                        request.getImageUrls() == null
                                ? new ArrayList<>()
                                : new ArrayList<>(
                                request.getImageUrls()
                        )
                )
                .build();
    }
    /*
     * ============================================================
     * EXTRACT ITINERARY LOCATIONS
     * ============================================================
     */
    private List<String> extractItineraryLocations(
            Itinerary itinerary
    ) {

        List<String> locations =
                new ArrayList<>();

        if (itinerary == null
                || itinerary.getDays() == null) {

            return locations;
        }

        for (var day :
                itinerary.getDays()) {

            if (day == null
                    || day.getActivities() == null) {

                continue;
            }

            for (var activity :
                    day.getActivities()) {

                if (activity == null) {
                    continue;
                }

                String location =
                        activity.getLocation();

                if (location == null
                        || location.isBlank()) {

                    continue;
                }

                String cleanedLocation =
                        location.trim();

                /*
                 * Only remove consecutive duplicates.
                 */
                if (locations.isEmpty()
                        || !locations.get(
                        locations.size() - 1
                ).equalsIgnoreCase(
                        cleanedLocation
                )) {

                    locations.add(
                            cleanedLocation
                    );
                }
            }
        }

        return locations;
    }


    /*
     * ============================================================
     * BUILD USER SELECTION CONTEXT
     * ============================================================
     */
    private String buildSelectionContext(
            HotelData selectedHotel,
            List<PlaceData> selectedPlaces,
            String additionalPreferences
    ) {

        StringBuilder context =
                new StringBuilder();

        context.append(
                "\n\nUSER-CONFIRMED SELECTIONS\n"
        );


        /*
         * --------------------------------------------------------
         * SELECTED HOTEL
         * --------------------------------------------------------
         */
        if (selectedHotel != null) {

            context.append(
                    "Selected hotel:\n"
            );

            context.append(
                    "Property Token: "
            );

            context.append(
                    selectedHotel.getPropertyToken()
            );

            context.append("\n");

            context.append(
                    "Hotel Name: "
            );

            context.append(
                    selectedHotel.getName()
            );

            context.append("\n");

            if (selectedHotel.getAddress() != null
                    && !selectedHotel.getAddress().isBlank()) {

                context.append(
                        "Hotel Address: "
                );

                context.append(
                        selectedHotel.getAddress()
                );

                context.append("\n");
            }

            if (selectedHotel.getTotalPrice() != null) {

                context.append(
                        "Total Hotel Cost: "
                );

                context.append(
                        selectedHotel.getTotalPrice()
                );

                context.append("\n");
            }

            if (selectedHotel.getNightlyPrice() != null) {

                context.append(
                        "Nightly Hotel Cost: "
                );

                context.append(
                        selectedHotel.getNightlyPrice()
                );

                context.append("\n");
            }

            context.append(
                    "\nHOTEL LOCATION RULE:\n"
            );

            context.append(
                    "When referring to the selected hotel as a location, "
                            + "ALWAYS use this exact hotel name: "
            );

            context.append(
                    selectedHotel.getName()
            );

            context.append("\n");

            context.append(
                    "NEVER use generic location names such as "
                            + "\"Hotel\", \"Resort\", or \"Accommodation\".\n"
            );

        } else {

            context.append(
                    "No hotel was selected.\n"
            );
        }


        /*
         * --------------------------------------------------------
         * SELECTED PLACES
         * --------------------------------------------------------
         */
        if (selectedPlaces != null
                && !selectedPlaces.isEmpty()) {

            context.append(
                    "\nSelected places:\n"
            );

            for (PlaceData place :
                    selectedPlaces) {

                if (place == null) {
                    continue;
                }

                context.append("- ")
                        .append(
                                nullToEmpty(
                                        place.getId()
                                )
                        )
                        .append(" | ")
                        .append(
                                nullToEmpty(
                                        place.getName()
                                )
                        )
                        .append("\n");
            }

        } else {

            context.append(
                    "\nNo specific places were selected.\n"
            );
        }


        /*
         * --------------------------------------------------------
         * ADDITIONAL USER PREFERENCES
         * --------------------------------------------------------
         */
        if (additionalPreferences != null
                && !additionalPreferences.isBlank()) {

            context.append(
                    "\nAdditional user preferences:\n"
            );

            context.append(
                    additionalPreferences
            );

            context.append("\n");
        }


        /*
         * --------------------------------------------------------
         * HARD AI RULES
         * --------------------------------------------------------
         */
        context.append(
                """
                
                IMPORTANT INSTRUCTIONS:

                1. The selected hotel is confirmed by the user.
                2. The selected places are confirmed by the user.
                3. Do not introduce new attractions, beaches,
                   restaurants, cafes, or sightseeing locations.
                4. Use the exact selected hotel name whenever the
                   hotel is used as an activity location.
                5. Never use generic hotel names such as "Hotel",
                   "Resort", or "Accommodation".
                6. Use the exact selected place names supplied above.
                7. You may repeat a selected place when appropriate.
                8. You may use the selected hotel for check-in,
                   check-out, breakfast, lunch, dinner, or rest.
                9. Every day must contain a non-null "notes" string.
                10. If there are no notes, return "" instead of null.
                11. Return valid JSON only.
                12. Do not return markdown.
                13. Every activity location must be either:
                    a confirmed selected place, or
                    the exact selected hotel name.
                """
        );

        return context.toString();
    }


    /*
     * ============================================================
     * NULL -> EMPTY STRING
     * ============================================================
     */
    private String nullToEmpty(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }


    /*
     * ============================================================
     * SELECTED REQUEST VALIDATION
     * ============================================================
     */
    private void validateSelectedRequest(
            GenerateSelectedItineraryRequest request
    ) {

        if (request == null) {

            throw new BadRequestException(
                    "Selected itinerary generation request is required."
            );
        }

        if (request.getTripId() == null) {

            throw new BadRequestException(
                    "Trip ID is required."
            );
        }

        if (request.getCurrency() == null
                || !request.getCurrency()
                .matches("[A-Za-z]{3}")) {

            throw new BadRequestException(
                    "Currency must be a valid 3-letter currency code."
            );
        }

        if (request.getSelectedPlaceIds() == null
                || request.getSelectedPlaceIds().isEmpty()) {

            throw new BadRequestException(
                    "At least one selected place is required."
            );
        }
    }


    /*
     * ============================================================
     * NORMAL REQUEST VALIDATION
     * ============================================================
     */
    private void validateRequest(
            GenerateItineraryRequest request,
            Trip trip
    ) {

        if (request == null) {

            throw new BadRequestException(
                    "Itinerary generation request is required."
            );
        }

        if (request.getNumberOfDays() == null) {

            throw new BadRequestException(
                    "Number of itinerary days is required."
            );
        }

        if (request.getCurrency() == null
                || !request.getCurrency()
                .matches("[A-Za-z]{3}")) {

            throw new BadRequestException(
                    "Currency must be a valid 3-letter currency code."
            );
        }

        long tripDuration =
                ChronoUnit.DAYS.between(
                        trip.getStartDate(),
                        trip.getEndDate()
                ) + 1;

        if (request.getNumberOfDays() > tripDuration) {

            throw new BadRequestException(
                    "Requested itinerary days cannot exceed the trip duration."
            );
        }
    }
}