package com.OdysseyAi.demo.trip.repository;



import com.OdysseyAi.demo.trip.entity.Itinerary;
import com.OdysseyAi.demo.trip.entity.ItineraryDay;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ItineraryDayRepository
        extends JpaRepository<ItineraryDay, Long> {

    @EntityGraph(attributePaths = "activities")
    List<ItineraryDay> findAllByItineraryOrderByDayNumberAsc(
            Itinerary itinerary
    );

    Optional<ItineraryDay> findByIdAndItinerary(
            Long id,
            Itinerary itinerary
    );
}