package com.OdysseyAi.demo.trip.interfaces;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.common.enums.TripMemberRole;
import com.OdysseyAi.demo.trip.entity.Trip;

public interface TripAccessService {

    void requireOwner(
            Trip trip,
            User user
    );

    void requireViewer(
            Trip trip,
            User user
    );

    void requireEditor(
            Trip trip,
            User user
    );

    TripMemberRole getRole(
            Trip trip,
            User user
    );
}