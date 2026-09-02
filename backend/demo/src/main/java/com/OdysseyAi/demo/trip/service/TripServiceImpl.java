package com.OdysseyAi.demo.trip.service;


import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.interfaces.AuthenticationFacade;
import com.OdysseyAi.demo.common.enums.TripStatus;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.common.exception.customexception.ResourceNotFoundException;
import com.OdysseyAi.demo.trip.dto.request.CreateTripRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateTripRequest;
import com.OdysseyAi.demo.trip.dto.response.PageResponse;
import com.OdysseyAi.demo.trip.dto.response.TripResponse;
import com.OdysseyAi.demo.trip.entity.Trip;
import com.OdysseyAi.demo.trip.mapper.TripMapper;
import com.OdysseyAi.demo.trip.repository.TripRepository;
import com.OdysseyAi.demo.trip.interfaces.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public PageResponse<TripResponse> getMyTrips(
            int page,
            int size,
            String sortBy,
            String direction,
            TripStatus status,
            String destination
    ) {

        User currentUser = authenticationFacade.getCurrentUser();

        if (page < 0) {
            throw new BadRequestException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException(
                    "Page size must be between 1 and 100."
            );
        }

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt";
        }

        if (direction == null || direction.isBlank()) {
            direction = "desc";
        }

        String validatedSortField = validateSortField(sortBy);

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Sort direction must be either 'asc' or 'desc'."
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, validatedSortField)
        );

        boolean hasStatus = status != null;

        boolean hasDestination =
                destination != null &&
                        !destination.isBlank();

        Page<Trip> tripPage;

        if (hasStatus && hasDestination) {

            tripPage =
                    tripRepository.findAccessibleTripsByStatusAndDestination(
                            currentUser,
                            status,
                            destination.trim(),
                            pageable
                    );

        } else if (hasStatus) {

            tripPage =
                    tripRepository.findAccessibleTripsByStatus(
                            currentUser,
                            status,
                            pageable
                    );

        } else if (hasDestination) {

            tripPage =
                    tripRepository.findAccessibleTripsByDestination(
                            currentUser,
                            destination.trim(),
                            pageable
                    );

        } else {

            tripPage =
                    tripRepository.findAccessibleTrips(
                            currentUser,
                            pageable
                    );
        }

        List<TripResponse> content =
                tripPage.getContent()
                        .stream()
                        .map(tripMapper::toResponse)
                        .toList();

        return PageResponse.<TripResponse>builder()
                .content(content)
                .page(tripPage.getNumber())
                .size(tripPage.getSize())
                .totalElements(tripPage.getTotalElements())
                .totalPages(tripPage.getTotalPages())
                .first(tripPage.isFirst())
                .last(tripPage.isLast())
                .build();
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

    private String validateSortField(String sortBy) {

        return switch (sortBy) {

            case "createdAt",
                 "updatedAt",
                 "title",
                 "destination",
                 "startDate",
                 "endDate",
                 "status" -> sortBy;

            default -> throw new BadRequestException(
                    "Invalid sort field."
            );
        };
    }
}
