package com.OdysseyAi.demo.trip.service;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.interfaces.AuthenticationFacade;
import com.OdysseyAi.demo.auth.repository.UserRepository;
import com.OdysseyAi.demo.common.enums.TripMemberRole;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.common.exception.customexception.ForbiddenException;
import com.OdysseyAi.demo.common.exception.customexception.ResourceNotFoundException;
import com.OdysseyAi.demo.trip.dto.request.AddTripMemberRequest;
import com.OdysseyAi.demo.trip.dto.response.TripMemberResponse;
import com.OdysseyAi.demo.trip.entity.Trip;
import com.OdysseyAi.demo.trip.entity.TripMember;
import com.OdysseyAi.demo.trip.interfaces.TripMemberService;
import com.OdysseyAi.demo.trip.repository.TripMemberRepository;
import com.OdysseyAi.demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripMemberServiceImpl implements TripMemberService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public TripMemberResponse addMember(
            Long tripId,
            AddTripMemberRequest request
    ) {

        User owner = authenticationFacade.getCurrentUser();

        Trip trip = findOwnedTrip(tripId, owner);

        User user = userRepository
                .findUserByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with email '" + request.getEmail() + "' was not found."
                        )
                );

        if (trip.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(
                    "The trip owner is already the organizer of this trip."
            );
        }

        if (tripMemberRepository.existsByTripAndUser(trip, user)) {
            throw new BadRequestException(
                    "User '" + request.getEmail() + "' is already a member of this trip."
            );
        }

        TripMember member = TripMember.builder()
                .trip(trip)
                .user(user)
                .role(request.getRole())
                .build();

        TripMember savedMember =
                tripMemberRepository.save(member);

        return toResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripMemberResponse> getMembers(Long tripId) {

        User currentUser = authenticationFacade.getCurrentUser();

        Trip trip = tripRepository.findAccessibleTripById(tripId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found."));

        return tripMemberRepository
                .findAllByTrip(trip)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void updateMemberRole(
            Long tripId,
            Long memberId,
            TripMemberRole role
    ) {

        User owner = authenticationFacade.getCurrentUser();

        Trip trip = findOwnedTrip(tripId, owner);

        TripMember member =
                tripMemberRepository
                        .findById(memberId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Trip member not found."
                                ));

        if (!member.getTrip().getId().equals(trip.getId())) {
            throw new ResourceNotFoundException(
                    "Trip member not found."
            );
        }

        member.changeRole(role);
    }

    @Override
    public void removeMember(
            Long tripId,
            Long memberId
    ) {

        User currentUser = authenticationFacade.getCurrentUser();

        Trip trip = tripRepository.findAccessibleTripById(tripId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found."));

        TripMember member =
                tripMemberRepository
                        .findById(memberId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Trip member not found."
                                ));

        if (!member.getTrip().getId().equals(trip.getId())) {
            throw new ResourceNotFoundException(
                    "Trip member not found."
            );
        }

        boolean isOwner = trip.getUser().getId().equals(currentUser.getId());
        boolean isSelf = member.getUser().getId().equals(currentUser.getId());

        if (!isOwner && !isSelf) {
            throw new ForbiddenException(
                    "Only the trip owner can remove members from this trip."
            );
        }

        tripMemberRepository.delete(member);
    }

    private Trip findOwnedTrip(
            Long tripId,
            User currentUser
    ) {

        return tripRepository
                .findByIdAndUser(tripId, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trip not found."
                        ));
    }

    private TripMemberResponse toResponse(
            TripMember member
    ) {

        User user = member.getUser();

        return TripMemberResponse.builder()
                .id(member.getId())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(member.getRole())
                .build();
    }
}