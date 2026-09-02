package com.OdysseyAi.demo.trip.interfaces;

import com.OdysseyAi.demo.common.enums.TripMemberRole;
import com.OdysseyAi.demo.trip.dto.request.AddTripMemberRequest;
import com.OdysseyAi.demo.trip.dto.response.TripMemberResponse;

import java.util.List;

public interface TripMemberService {

    TripMemberResponse addMember(
            Long tripId,
            AddTripMemberRequest request
    );

    List<TripMemberResponse> getMembers(Long tripId);

    void updateMemberRole(
            Long tripId,
            Long memberId,
            TripMemberRole role
    );

    void removeMember(
            Long tripId,
            Long memberId
    );
}