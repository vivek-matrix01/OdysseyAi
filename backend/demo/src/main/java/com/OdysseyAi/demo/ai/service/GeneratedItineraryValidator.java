package com.OdysseyAi.demo.ai.service;

import com.OdysseyAi.demo.ai.dto.response.GeneratedActivityResponse;
import com.OdysseyAi.demo.ai.dto.response.GeneratedItineraryDayResponse;
import com.OdysseyAi.demo.ai.dto.response.GeneratedItineraryResponse;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.trip.entity.Trip;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class GeneratedItineraryValidator {

    public void validate(
            GeneratedItineraryResponse response,
            Trip trip,
            int requestedDays
    ) {

        if (response == null) {
            throw new BadRequestException(
                    "AI returned an empty itinerary."
            );
        }

        List<GeneratedItineraryDayResponse> days =
                response.getDays();

        if (days == null || days.isEmpty()) {
            throw new BadRequestException(
                    "AI returned no itinerary days."
            );
        }

        if (days.size() != requestedDays) {
            throw new BadRequestException(
                    "AI returned an incorrect number of itinerary days."
            );
        }

        Set<Integer> dayNumbers = new HashSet<>();

        LocalDate previousDate = null;

        for (int index = 0;
             index < days.size();
             index++) {

            GeneratedItineraryDayResponse day =
                    days.get(index);

            validateDay(
                    day,
                    trip,
                    requestedDays,
                    index,
                    dayNumbers,
                    previousDate
            );

            previousDate = day.getDate();
        }
    }

    private void validateDay(
            GeneratedItineraryDayResponse day,
            Trip trip,
            int requestedDays,
            int index,
            Set<Integer> dayNumbers,
            LocalDate previousDate
    ) {

        if (day == null) {
            throw new BadRequestException(
                    "AI returned a null itinerary day."
            );
        }

        Integer expectedDayNumber = index + 1;

        if (day.getDayNumber() == null
                || !day.getDayNumber()
                .equals(expectedDayNumber)) {

            throw new BadRequestException(
                    "AI itinerary day numbers must be sequential starting from 1."
            );
        }

        if (!dayNumbers.add(day.getDayNumber())) {
            throw new BadRequestException(
                    "AI itinerary contains duplicate day numbers."
            );
        }

        validateDate(
                day.getDate(),
                trip,
                previousDate
        );

        validateActivities(
                day.getActivities()
        );
    }

    private void validateDate(
            LocalDate date,
            Trip trip,
            LocalDate previousDate
    ) {

        if (date == null) {
            throw new BadRequestException(
                    "AI itinerary day date is required."
            );
        }

        if (date.isBefore(trip.getStartDate())
                || date.isAfter(trip.getEndDate())) {

            throw new BadRequestException(
                    "AI itinerary contains a date outside the Trip dates."
            );
        }

        if (previousDate != null
                && !date.isAfter(previousDate)) {

            throw new BadRequestException(
                    "AI itinerary dates must be in ascending order."
            );
        }
    }

    private void validateActivities(
            List<GeneratedActivityResponse> activities
    ) {

        if (activities == null) {
            throw new BadRequestException(
                    "AI itinerary contains a null activities list."
            );
        }

        for (GeneratedActivityResponse activity :
                activities) {

            validateActivity(activity);
        }
    }

    private void validateActivity(
            GeneratedActivityResponse activity
    ) {

        if (activity == null) {
            throw new BadRequestException(
                    "AI itinerary contains a null activity."
            );
        }

        if (activity.getTitle() == null
                || activity.getTitle().isBlank()) {

            throw new BadRequestException(
                    "AI itinerary activity title is required."
            );
        }

        LocalTime startTime =
                activity.getStartTime();

        LocalTime endTime =
                activity.getEndTime();

        if (startTime != null
                && endTime != null
                && !endTime.isAfter(startTime)) {

            throw new BadRequestException(
                    "AI itinerary activity end time must be after start time."
            );
        }

        BigDecimal estimatedCost =
                activity.getEstimatedCost();

        if (estimatedCost != null
                && estimatedCost.signum() < 0) {

            throw new BadRequestException(
                    "AI itinerary activity cost cannot be negative."
            );
        }

        if (activity.getCurrency() != null
                && (
                activity.getCurrency().length() != 3
                        || !activity.getCurrency()
                        .matches("[A-Za-z]{3}")
        )) {

            throw new BadRequestException(
                    "AI itinerary activity currency must be a 3-letter code."
            );
        }

        if (activity.getType() == null) {
            throw new BadRequestException(
                    "AI itinerary activity type is required."
            );
        }
    }
}