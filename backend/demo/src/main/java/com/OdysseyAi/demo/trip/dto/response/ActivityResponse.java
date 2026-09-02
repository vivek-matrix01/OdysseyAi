package com.OdysseyAi.demo.trip.dto.response;


import com.OdysseyAi.demo.common.enums.ActivityType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityResponse {

    private Long id;

    private String title;

    private String description;

    private LocalTime startTime;

    private LocalTime endTime;

    private String location;

    private BigDecimal estimatedCost;

    private String currency;

    private ActivityType type;
}