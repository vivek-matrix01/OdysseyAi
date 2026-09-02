package com.OdysseyAi.demo.ai.mapper;

import com.OdysseyAi.demo.ai.dto.response.GeneratedActivityResponse;
import com.OdysseyAi.demo.ai.dto.response.GeneratedItineraryDayResponse;
import com.OdysseyAi.demo.ai.dto.response.GeneratedItineraryResponse;
import com.OdysseyAi.demo.trip.entity.Activity;
import com.OdysseyAi.demo.trip.entity.Itinerary;
import com.OdysseyAi.demo.trip.entity.ItineraryDay;
import org.springframework.stereotype.Component;

@Component
public class AiItineraryMapper {

    public Itinerary toEntity(
            GeneratedItineraryResponse response
    ) {

        Itinerary itinerary =
                Itinerary.builder()
                        .summary("AI generated itinerary")
                        .build();

        for (GeneratedItineraryDayResponse dayResponse :
                response.getDays()) {

            ItineraryDay day =
                    toDayEntity(dayResponse);

            itinerary.addDay(day);
        }

        return itinerary;
    }

    private ItineraryDay toDayEntity(
            GeneratedItineraryDayResponse dayResponse
    ) {

        ItineraryDay day =
                ItineraryDay.builder()
                        .dayNumber(dayResponse.getDayNumber())
                        .date(dayResponse.getDate())
                        .notes(dayResponse.getNotes())
                        .build();

        for (GeneratedActivityResponse activityResponse :
                dayResponse.getActivities()) {

            Activity activity =
                    toActivityEntity(activityResponse);

            day.addActivity(activity);
        }

        return day;
    }

    private Activity toActivityEntity(
            GeneratedActivityResponse activityResponse
    ) {

        return Activity.builder()
                .title(activityResponse.getTitle())
                .description(activityResponse.getDescription())
                .startTime(activityResponse.getStartTime())
                .endTime(activityResponse.getEndTime())
                .location(activityResponse.getLocation())
                .estimatedCost(activityResponse.getEstimatedCost())
                .currency(activityResponse.getCurrency())
                .type(activityResponse.getType())
                .build();
    }
}