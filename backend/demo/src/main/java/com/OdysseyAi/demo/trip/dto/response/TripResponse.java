package com.OdysseyAi.demo.trip.dto.response;

import com.OdysseyAi.demo.common.enums.TripStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
public class TripResponse {

    private Long id;
    private String title;
    private String description;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private TripStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}