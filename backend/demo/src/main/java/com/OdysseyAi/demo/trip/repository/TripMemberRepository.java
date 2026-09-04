package com.OdysseyAi.demo.trip.repository;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.trip.entity.Trip;
import com.OdysseyAi.demo.trip.entity.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripMemberRepository
        extends JpaRepository<TripMember, Long> {

    Optional<TripMember> findByTripAndUser(
            Trip trip,
            User user
    );

    boolean existsByTripAndUser(
            Trip trip,
            User user
    );

    List<TripMember> findAllByTrip(Trip trip);

    void deleteByTripAndUser(
            Trip trip,
            User user
    );
    Optional<TripMember> findByTripAndUserId(
            Trip trip,
            Long userId
    );

    void deleteByTrip(Trip trip);
}