package com.OdysseyAi.demo.ai.dto.response;

import com.OdysseyAi.demo.common.enums.ActivityType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedActivityResponse {

    private String title;

    private String description;

    private LocalTime startTime;

    private LocalTime endTime;

    private String location;

    private BigDecimal estimatedCost;

    private String currency;

    private ActivityType type;
}