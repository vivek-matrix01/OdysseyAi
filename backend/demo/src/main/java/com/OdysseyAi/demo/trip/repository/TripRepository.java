package com.OdysseyAi.demo.trip.repository;



import com.OdysseyAi.demo.auth.entity.User;

import com.OdysseyAi.demo.trip.enitity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<Trip> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}
