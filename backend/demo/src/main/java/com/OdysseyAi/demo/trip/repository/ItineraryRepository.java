package com.OdysseyAi.demo.trip.repository;

import com.OdysseyAi.demo.trip.entity.Itinerary;
import com.OdysseyAi.demo.trip.entity.Trip;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    @EntityGraph(attributePaths = "days")
    Optional<Itinerary> findByTrip(Trip trip);

    boolean existsByTrip(Trip trip);


    Optional<Itinerary> findWithDaysAndActivitiesByTrip(Trip trip);
}