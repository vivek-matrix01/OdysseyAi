package com.OdysseyAi.demo.trip.service;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.interfaces.AuthenticationFacade;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.common.exception.customexception.ResourceNotFoundException;
import com.OdysseyAi.demo.trip.dto.request.CreateActivityRequest;
import com.OdysseyAi.demo.trip.dto.request.CreateItineraryDayRequest;
import com.OdysseyAi.demo.trip.dto.request.CreateItineraryRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateActivityRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateItineraryDayRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateItineraryRequest;
import com.OdysseyAi.demo.trip.dto.response.ActivityResponse;
import com.OdysseyAi.demo.trip.dto.response.ItineraryDayResponse;
import com.OdysseyAi.demo.trip.dto.response.ItineraryResponse;
import com.OdysseyAi.demo.trip.entity.Activity;
import com.OdysseyAi.demo.trip.entity.Itinerary;
import com.OdysseyAi.demo.trip.entity.ItineraryDay;
import com.OdysseyAi.demo.trip.entity.Trip;
import com.OdysseyAi.demo.trip.interfaces.ItineraryService;
import com.OdysseyAi.demo.trip.interfaces.TripAccessService;
import com.OdysseyAi.demo.trip.mapper.ItineraryMapper;
import com.OdysseyAi.demo.trip.repository.ActivityRepository;
import com.OdysseyAi.demo.trip.repository.ItineraryDayRepository;
import com.OdysseyAi.demo.trip.repository.ItineraryRepository;
import com.OdysseyAi.demo.trip.repository.TripRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryServiceImpl implements ItineraryService {

    private final TripRepository tripRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryDayRepository itineraryDayRepository;
    private final AuthenticationFacade authenticationFacade;
    private final ItineraryMapper itineraryMapper;
    private final EntityManager entityManager;
    private final ActivityRepository activityRepository;
    private final TripAccessService tripAccessService;


    /*
     * ============================================================
     * CREATE ITINERARY
     * ============================================================
     */

    @Override
    @Transactional
    public ItineraryResponse createItinerary(
            Long tripId,
            CreateItineraryRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    "Itinerary request is required."
            );
        }

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

        if (itineraryRepository.existsByTrip(trip)) {
            throw new BadRequestException(
                    "An itinerary already exists for this trip."
            );
        }

        validateItinerary(
                request.getDays(),
                trip
        );

        Itinerary itinerary =
                itineraryMapper.toEntity(request);

        itinerary.assignTrip(trip);

        for (
                CreateItineraryDayRequest dayRequest
                : request.getDays()
        ) {

            ItineraryDay day =
                    itineraryMapper.toEntity(
                            dayRequest
                    );

            for (
                    CreateActivityRequest activityRequest
                    : dayRequest.getActivities()
            ) {

                Activity activity =
                        itineraryMapper.toEntity(
                                activityRequest
                        );

                day.addActivity(activity);
            }

            itinerary.addDay(day);
        }

        BigDecimal activityCost =
                calculateActivityCost(itinerary);

        itinerary.updateRoutes(
                List.of()
        );

        itinerary.updateCostBreakdown(
                BigDecimal.ZERO,
                activityCost,
                BigDecimal.ZERO,
                activityCost
        );

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
     * GET ITINERARY
     * ============================================================
     */

    @Override
    @Transactional(readOnly = true)
    public ItineraryResponse getItinerary(
            Long tripId
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

        tripAccessService.requireViewer(
                trip,
                currentUser
        );

        Itinerary itinerary =
                itineraryRepository.findByTrip(trip)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary not found."
                                )
                        );

        List<ItineraryDay> days =
                itineraryDayRepository
                        .findAllByItineraryOrderByDayNumberAsc(
                                itinerary
                        );

        itinerary.replaceDays(days);

        return itineraryMapper.toResponse(
                itinerary
        );
    }


    /*
     * ============================================================
     * UPDATE ENTIRE ITINERARY
     * ============================================================
     */

    @Override
    @Transactional
    public ItineraryResponse updateItinerary(
            Long tripId,
            UpdateItineraryRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    "Itinerary update request is required."
            );
        }

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

        Itinerary itinerary =
                itineraryRepository.findByTrip(trip)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary not found."
                                )
                        );

        validateItinerary(
                request.getDays(),
                trip
        );

        itinerary.updateSummary(
                request.getSummary()
        );

        itinerary.clearDays();

        /*
         * Because orphanRemoval is enabled, flush old children
         * before inserting replacement children.
         */
        entityManager.flush();

        for (
                CreateItineraryDayRequest dayRequest
                : request.getDays()
        ) {

            ItineraryDay day =
                    itineraryMapper.toEntity(
                            dayRequest
                    );

            for (
                    CreateActivityRequest activityRequest
                    : dayRequest.getActivities()
            ) {

                Activity activity =
                        itineraryMapper.toEntity(
                                activityRequest
                        );

                day.addActivity(activity);
            }

            itinerary.addDay(day);
        }

        BigDecimal activityCost =
                calculateActivityCost(itinerary);

        /*
         * Existing route data becomes invalid when the itinerary
         * structure is manually changed.
         */
        itinerary.updateRoutes(
                List.of()
        );

        BigDecimal hotelCost =
                safeAmount(
                        itinerary.getEstimatedHotelCost()
                );

        BigDecimal transportCost =
                BigDecimal.ZERO;

        BigDecimal totalCost =
                hotelCost
                        .add(activityCost)
                        .add(transportCost);

        itinerary.updateCostBreakdown(
                hotelCost,
                activityCost,
                transportCost,
                totalCost
        );

        itinerary.markModified();

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
     * ADD DAY
     * ============================================================
     */

    @Override
    @Transactional
    public ItineraryDayResponse addDay(
            Long tripId,
            CreateItineraryDayRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    "Itinerary day request is required."
            );
        }

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

        Itinerary itinerary =
                itineraryRepository.findByTrip(trip)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary not found."
                                )
                        );

        validateDayRequest(
                request,
                trip
        );

        long tripDuration =
                calculateTripDuration(trip);

        if (request.getDayNumber() > tripDuration) {
            throw new BadRequestException(
                    "Day number cannot exceed the trip duration."
            );
        }

        boolean dayExists =
                itinerary.getDays()
                        .stream()
                        .anyMatch(day ->
                                request.getDayNumber()
                                        .equals(day.getDayNumber())
                        );

        if (dayExists) {
            throw new BadRequestException(
                    "Day "
                            + request.getDayNumber()
                            + " already exists in the itinerary."
            );
        }

        ItineraryDay day =
                itineraryMapper.toEntity(
                        request
                );

        for (
                CreateActivityRequest activityRequest
                : request.getActivities()
        ) {

            Activity activity =
                    itineraryMapper.toEntity(
                            activityRequest
                    );

            day.addActivity(activity);
        }

        itinerary.addDay(day);

        /*
         * Day structure changed, so existing routes are no longer valid.
         */
        itinerary.updateRoutes(
                List.of()
        );

        recalculateCostsAfterStructureChange(
                itinerary
        );

        itinerary.markModified();

        itineraryRepository.save(
                itinerary
        );

        /*
         * Force Hibernate to execute INSERT statements now.
         * This ensures generated IDs are available before mapping
         * the response.
         */
        entityManager.flush();

        return itineraryMapper.toResponse(
                day
        );
    }

    /*
     * ============================================================
     * UPDATE DAY
     * ============================================================
     */

    @Override
    @Transactional
    public ItineraryDayResponse updateDay(
            Long tripId,
            Long dayId,
            UpdateItineraryDayRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    "Itinerary day update request is required."
            );
        }

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

        Itinerary itinerary =
                itineraryRepository.findByTrip(trip)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary not found."
                                )
                        );

        ItineraryDay day =
                itineraryDayRepository.findById(dayId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary day not found."
                                )
                        );

        requireDayBelongsToItinerary(
                day,
                itinerary
        );

        validateDayDate(
                request.getDate(),
                trip
        );

        day.updateDate(
                request.getDate()
        );

        day.updateNotes(
                request.getNotes()
        );

        recalculateActivityAndTotalCost(
                itinerary
        );

        itinerary.markModified();

        itineraryRepository.save(
                itinerary
        );

        return itineraryMapper.toResponse(
                day
        );
    }


    /*
     * ============================================================
     * DELETE DAY
     * ============================================================
     */

    @Override
    @Transactional
    public void deleteDay(
            Long tripId,
            Long dayId
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

        Itinerary itinerary =
                itineraryRepository.findByTrip(trip)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary not found."
                                )
                        );

        ItineraryDay day =
                itineraryDayRepository.findById(dayId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary day not found."
                                )
                        );

        requireDayBelongsToItinerary(
                day,
                itinerary
        );

        if (itinerary.getDays().size() <= 1) {

            throw new BadRequestException(
                    "An itinerary must contain at least one day."
            );
        }

        itinerary.removeDay(
                day
        );

        entityManager.flush();

        itinerary.updateRoutes(
                List.of()
        );

        recalculateCostsAfterStructureChange(
                itinerary
        );

        itinerary.markModified();

        itineraryRepository.save(
                itinerary
        );
    }


    /*
     * ============================================================
     * ADD ACTIVITY
     * ============================================================
     */

    @Override
    @Transactional
    public ActivityResponse addActivity(
            Long tripId,
            Long dayId,
            CreateActivityRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    "Activity request is required."
            );
        }

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

        Itinerary itinerary =
                itineraryRepository.findByTrip(trip)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary not found."
                                )
                        );

        ItineraryDay day =
                itineraryDayRepository.findById(dayId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary day not found."
                                )
                        );

        requireDayBelongsToItinerary(
                day,
                itinerary
        );

        /*
         * IMPORTANT:
         *
         * Explicitly persist the Activity first.
         * The Activity owns the ManyToOne relationship to the day.
         */
        Activity activity =
                itineraryMapper.toEntity(
                        request
                );

        activity.assignItineraryDay(day);

        Activity savedActivity =
                activityRepository.save(
                        activity
                );

        /*
         * Add the already-persisted activity to the inverse
         * collection.
         */
        day.getActivities().add(
                savedActivity
        );

        /*
         * Adding an activity changes the actual travel sequence,
         * so old route data must be discarded.
         */
        itinerary.updateRoutes(
                List.of()
        );

        recalculateCostsAfterStructureChange(
                itinerary
        );

        itinerary.markModified();

        /*
         * Do NOT call itineraryRepository.save(itinerary) here.
         *
         * trip, itinerary and day are already managed because this
         * method is transactional. Hibernate will automatically
         * persist the changed cost/status fields when the transaction
         * flushes.
         */

        entityManager.flush();

        return itineraryMapper.toResponse(
                savedActivity
        );
    }


    /*
     * ============================================================
     * UPDATE ACTIVITY
     * ============================================================
     */

    @Override
    @Transactional
    public ActivityResponse updateActivity(
            Long tripId,
            Long dayId,
            Long activityId,
            UpdateActivityRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    "Activity update request is required."
            );
        }

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

        Itinerary itinerary =
                itineraryRepository.findByTrip(trip)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary not found."
                                )
                        );

        ItineraryDay day =
                itineraryDayRepository.findById(dayId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary day not found."
                                )
                        );

        requireDayBelongsToItinerary(
                day,
                itinerary
        );

        Activity activity =
                activityRepository.findById(activityId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Activity not found."
                                )
                        );

        requireActivityBelongsToDay(
                activity,
                day
        );

        activity.updateDetails(
                request.getTitle(),
                request.getDescription(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLocation(),
                request.getEstimatedCost(),
                request.getCurrency(),
                request.getType()
        );

        itinerary.updateRoutes(
                List.of()
        );

        recalculateCostsAfterStructureChange(
                itinerary
        );

        itinerary.markModified();

        itineraryRepository.save(
                itinerary
        );

        return itineraryMapper.toResponse(
                activity
        );
    }


    /*
     * ============================================================
     * DELETE ACTIVITY
     * ============================================================
     */

    @Override
    @Transactional
    public void deleteActivity(
            Long tripId,
            Long dayId,
            Long activityId
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

        Itinerary itinerary =
                itineraryRepository.findByTrip(trip)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary not found."
                                )
                        );

        ItineraryDay day =
                itineraryDayRepository.findById(dayId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Itinerary day not found."
                                )
                        );

        requireDayBelongsToItinerary(
                day,
                itinerary
        );

        Activity activity =
                activityRepository.findById(activityId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Activity not found."
                                )
                        );

        requireActivityBelongsToDay(
                activity,
                day
        );

        day.removeActivity(
                activity
        );

        entityManager.flush();

        itinerary.updateRoutes(
                List.of()
        );

        recalculateCostsAfterStructureChange(
                itinerary
        );

        itinerary.markModified();

        itineraryRepository.save(
                itinerary
        );
    }


    /*
     * ============================================================
     * COST HELPERS
     * ============================================================
     */

    private void recalculateCostsAfterStructureChange(
            Itinerary itinerary
    ) {

        BigDecimal activityCost =
                calculateActivityCost(
                        itinerary
                );

        BigDecimal hotelCost =
                safeAmount(
                        itinerary.getEstimatedHotelCost()
                );

        BigDecimal transportCost =
                BigDecimal.ZERO;

        BigDecimal totalCost =
                hotelCost
                        .add(activityCost)
                        .add(transportCost);

        itinerary.updateCostBreakdown(
                hotelCost,
                activityCost,
                transportCost,
                totalCost
        );
    }


    private void recalculateActivityAndTotalCost(
            Itinerary itinerary
    ) {

        BigDecimal activityCost =
                calculateActivityCost(
                        itinerary
                );

        BigDecimal hotelCost =
                safeAmount(
                        itinerary.getEstimatedHotelCost()
                );

        BigDecimal transportCost =
                safeAmount(
                        itinerary.getEstimatedTransportCost()
                );

        BigDecimal totalCost =
                hotelCost
                        .add(activityCost)
                        .add(transportCost);

        itinerary.updateCostBreakdown(
                hotelCost,
                activityCost,
                transportCost,
                totalCost
        );
    }


    private BigDecimal calculateActivityCost(
            Itinerary itinerary
    ) {

        if (itinerary == null
                || itinerary.getDays() == null
                || itinerary.getDays().isEmpty()) {

            return BigDecimal.ZERO;
        }

        BigDecimal total =
                BigDecimal.ZERO;

        for (
                ItineraryDay day
                : itinerary.getDays()
        ) {

            if (day == null
                    || day.getActivities() == null) {

                continue;
            }

            for (
                    Activity activity
                    : day.getActivities()
            ) {

                if (activity == null) {
                    continue;
                }

                BigDecimal estimatedCost =
                        activity.getEstimatedCost();

                if (estimatedCost != null) {

                    total =
                            total.add(
                                    estimatedCost
                            );
                }
            }
        }

        return total;
    }


    private BigDecimal safeAmount(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }


    /*
     * ============================================================
     * VALIDATION
     * ============================================================
     */

    private void validateItinerary(
            List<CreateItineraryDayRequest> days,
            Trip trip
    ) {

        if (days == null || days.isEmpty()) {

            throw new BadRequestException(
                    "Itinerary must contain at least one day."
            );
        }

        if (trip == null
                || trip.getStartDate() == null
                || trip.getEndDate() == null) {

            throw new BadRequestException(
                    "Trip dates are required."
            );
        }

        long expectedDays =
                calculateTripDuration(
                        trip
                );

        if (days.size() != expectedDays) {

            throw new BadRequestException(
                    "Itinerary must contain exactly "
                            + expectedDays
                            + " days for this trip."
            );
        }

        for (
                int i = 0;
                i < days.size();
                i++
        ) {

            CreateItineraryDayRequest day =
                    days.get(i);

            if (day == null) {

                throw new BadRequestException(
                        "Itinerary day cannot be null."
                );
            }

            int expectedDayNumber =
                    i + 1;

            if (day.getDayNumber() == null) {

                throw new BadRequestException(
                        "Day number is required."
                );
            }

            if (
                    day.getDayNumber()
                            != expectedDayNumber
            ) {

                throw new BadRequestException(
                        "Invalid day number. Expected day "
                                + expectedDayNumber
                                + "."
                );
            }

            if (day.getDate() == null) {

                throw new BadRequestException(
                        "Date is required for day "
                                + expectedDayNumber
                                + "."
                );
            }

            validateDayDate(
                    day.getDate(),
                    trip
            );

            if (day.getActivities() == null) {

                throw new BadRequestException(
                        "Activities cannot be null for day "
                                + expectedDayNumber
                                + "."
                );
            }
        }
    }


    private void validateDayRequest(
            CreateItineraryDayRequest request,
            Trip trip
    ) {

        if (request.getDayNumber() == null) {

            throw new BadRequestException(
                    "Day number is required."
            );
        }

        if (request.getDate() == null) {

            throw new BadRequestException(
                    "Date is required."
            );
        }

        if (request.getActivities() == null) {

            throw new BadRequestException(
                    "Activities cannot be null."
            );
        }

        validateDayDate(
                request.getDate(),
                trip
        );
    }


    private void validateDayDate(
            java.time.LocalDate date,
            Trip trip
    ) {

        if (date.isBefore(trip.getStartDate())
                || date.isAfter(trip.getEndDate())) {

            throw new BadRequestException(
                    "Itinerary day date must fall within the trip dates."
            );
        }
    }


    private long calculateTripDuration(
            Trip trip
    ) {

        return ChronoUnit.DAYS.between(
                trip.getStartDate(),
                trip.getEndDate()
        ) + 1;
    }


    /*
     * ============================================================
     * OWNERSHIP CHECKS
     * ============================================================
     */

    private void requireDayBelongsToItinerary(
            ItineraryDay day,
            Itinerary itinerary
    ) {

        if (day.getItinerary() == null
                || day.getItinerary().getId() == null
                || itinerary.getId() == null
                || !day.getItinerary()
                .getId()
                .equals(itinerary.getId())) {

            throw new BadRequestException(
                    "The itinerary day does not belong to this itinerary."
            );
        }
    }


    private void requireActivityBelongsToDay(
            Activity activity,
            ItineraryDay day
    ) {

        if (activity.getItineraryDay() == null
                || activity.getItineraryDay().getId() == null
                || day.getId() == null
                || !activity.getItineraryDay()
                .getId()
                .equals(day.getId())) {

            throw new BadRequestException(
                    "The activity does not belong to this itinerary day."
            );
        }
    }
}