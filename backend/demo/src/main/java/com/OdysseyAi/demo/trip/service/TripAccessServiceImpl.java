package com.OdysseyAi.demo.trip.service;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.common.enums.TripMemberRole;
import com.OdysseyAi.demo.common.exception.customexception.ForbiddenException;
import com.OdysseyAi.demo.common.exception.customexception.ResourceNotFoundException;
import com.OdysseyAi.demo.trip.entity.Trip;
import com.OdysseyAi.demo.trip.entity.TripMember;
import com.OdysseyAi.demo.trip.interfaces.TripAccessService;
import com.OdysseyAi.demo.trip.repository.TripMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripAccessServiceImpl implements TripAccessService {

    private final TripMemberRepository tripMemberRepository;

    @Override
    public void requireOwner(
            Trip trip,
            User user
    ) {

        if (!trip.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(
                    "Only the trip owner can perform this operation."
            );
        }
    }

    @Override
    public void requireViewer(
            Trip trip,
            User user
    ) {

        if (isOwner(trip, user)) {
            return;
        }

        if (!tripMemberRepository.existsByTripAndUser(trip, user)) {
            throw new ForbiddenException(
                    "You do not have access to this trip."
            );
        }
    }

    @Override
    public void requireEditor(
            Trip trip,
            User user
    ) {

        if (isOwner(trip, user)) {
            return;
        }

        TripMember member = tripMemberRepository
                .findByTripAndUser(trip, user)
                .orElseThrow(() ->
                        new ForbiddenException(
                                "You do not have access to this trip."
                        )
                );

        if (member.getRole() != TripMemberRole.EDITOR) {
            throw new ForbiddenException(
                    "Editor permission is required for this operation."
            );
        }
    }

    @Override
    public TripMemberRole getRole(
            Trip trip,
            User user
    ) {

        if (isOwner(trip, user)) {
            return null;
        }

        return tripMemberRepository
                .findByTripAndUser(trip, user)
                .map(TripMember::getRole)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trip membership not found."
                        ));
    }

    private boolean isOwner(
            Trip trip,
            User user
    ) {
        return trip.getUser().getId().equals(user.getId());
    }
}