package com.OdysseyAi.demo.trip.repository;



import com.OdysseyAi.demo.auth.entity.User;

import com.OdysseyAi.demo.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;



import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.common.enums.TripStatus;
import com.OdysseyAi.demo.trip.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, Long> {

    Page<Trip> findAllByUser(
            User user,
            Pageable pageable
    );

    Page<Trip> findAllByUserAndStatus(
            User user,
            TripStatus status,
            Pageable pageable
    );

    Page<Trip> findAllByUserAndDestinationContainingIgnoreCase(
            User user,
            String destination,
            Pageable pageable
    );

    Page<Trip> findAllByUserAndStatusAndDestinationContainingIgnoreCase(
            User user,
            TripStatus status,
            String destination,
            Pageable pageable
    );

    Optional<Trip> findByIdAndUser(Long tripId, User currentUser);
    @Query("""
    SELECT DISTINCT t
    FROM Trip t
    LEFT JOIN TripMember tm
        ON tm.trip = t
       AND tm.user = :user
    WHERE t.user = :user
       OR tm.user = :user
""")
    Page<Trip> findAccessibleTrips(
            @Param("user") User user,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT t
    FROM Trip t
    LEFT JOIN TripMember tm
        ON tm.trip = t
       AND tm.user = :user
    WHERE (t.user = :user OR tm.user = :user)
      AND t.status = :status
""")
    Page<Trip> findAccessibleTripsByStatus(
            @Param("user") User user,
            @Param("status") TripStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT t
    FROM Trip t
    LEFT JOIN TripMember tm
        ON tm.trip = t
       AND tm.user = :user
    WHERE (t.user = :user OR tm.user = :user)
      AND LOWER(t.destination)
          LIKE LOWER(CONCAT('%', :destination, '%'))
""")
    Page<Trip> findAccessibleTripsByDestination(
            @Param("user") User user,
            @Param("destination") String destination,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT t
    FROM Trip t
    LEFT JOIN TripMember tm
        ON tm.trip = t
       AND tm.user = :user
    WHERE (t.user = :user OR tm.user = :user)
      AND t.status = :status
      AND LOWER(t.destination)
          LIKE LOWER(CONCAT('%', :destination, '%'))
""")
    Page<Trip> findAccessibleTripsByStatusAndDestination(
            @Param("user") User user,
            @Param("status") TripStatus status,
            @Param("destination") String destination,
            Pageable pageable
    );
}