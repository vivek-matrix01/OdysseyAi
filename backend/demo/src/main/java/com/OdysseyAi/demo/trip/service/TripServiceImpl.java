package com.OdysseyAi.demo.trip.service;


import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.interfaces.AuthenticationFacade;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.common.exception.customexception.ResourceNotFoundException;
import com.OdysseyAi.demo.trip.dto.request.CreateTripRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateTripRequest;
import com.OdysseyAi.demo.trip.dto.response.TripResponse;
import com.OdysseyAi.demo.trip.enitity.Trip;
import com.OdysseyAi.demo.trip.mapper.TripMapper;
import com.OdysseyAi.demo.trip.repository.TripRepository;
import com.OdysseyAi.demo.trip.interfaces.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public TripResponse createTrip(CreateTripRequest request) {
        validateTripDates(
                request.getStartDate(),
                request.getEndDate()
        );

        User currentUser = authenticationFacade.getCurrentUser();

        Trip trip = tripMapper.toEntity(request);

        trip.assignUser(currentUser);

        Trip savedTrip = tripRepository.save(trip);

        return tripMapper.toResponse(savedTrip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getMyTrips() {

        User currentUser = authenticationFacade.getCurrentUser();

        return tripRepository
                .findAllByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(tripMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getMyTrip(Long tripId) {

        User currentUser = authenticationFacade.getCurrentUser();

        Trip trip = findMyTrip(tripId, currentUser);

        return tripMapper.toResponse(trip);
    }

    @Override
    public void startTrip(Long tripId) {

        User currentUser = authenticationFacade.getCurrentUser();

        Trip trip = findMyTrip(tripId, currentUser);

        trip.start();
    }
    @Override
    public void completeTrip(Long tripId) {

        User currentUser = authenticationFacade.getCurrentUser();

        Trip trip = findMyTrip(tripId, currentUser);

        trip.complete();
    }
    @Override
    public void cancelTrip(Long tripId) {

        User currentUser = authenticationFacade.getCurrentUser();

        Trip trip = findMyTrip(tripId, currentUser);

        trip.cancel();
    }
    @Override
    public TripResponse updateMyTrip(
            Long tripId,
            UpdateTripRequest request
    ) {
        validateTripDates(
                request.getStartDate(),
                request.getEndDate()
        );

        User currentUser = authenticationFacade.getCurrentUser();

        Trip trip = findMyTrip(tripId, currentUser);

        tripMapper.updateEntity(request, trip);

        return tripMapper.toResponse(trip);
    }

    @Override
    public void deleteMyTrip(Long tripId) {

        User currentUser = authenticationFacade.getCurrentUser();

        Trip trip = findMyTrip(tripId, currentUser);

        tripRepository.delete(trip);
    }

    private Trip findMyTrip(Long tripId, User currentUser) {

        return tripRepository
                .findByIdAndUser(tripId, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trip not found."
                        )
                );
    }
    private void validateTripDates(
            LocalDate startDate,
            LocalDate endDate) {

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException(
                    "Trip end date cannot be before the start date."
            );
        }
    }
}
