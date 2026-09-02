package com.OdysseyAi.demo.trip.repository;


import com.OdysseyAi.demo.trip.entity.Activity;
import com.OdysseyAi.demo.trip.entity.ItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository
        extends JpaRepository<Activity, Long> {

    List<Activity> findAllByItineraryDayOrderByStartTimeAsc(
            ItineraryDay itineraryDay
    );

    Optional<Activity> findByIdAndItineraryDay(
            Long id,
            ItineraryDay itineraryDay
    );
}