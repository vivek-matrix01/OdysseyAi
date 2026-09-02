package com.OdysseyAi.demo.trip.entity;

import com.OdysseyAi.demo.common.entity.BaseEntity;
import com.OdysseyAi.demo.common.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "activities")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Activity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "itinerary_day_id",
            nullable = false
    )
    private ItineraryDay itineraryDay;

    @Setter
    @Column(nullable = false, length = 150)
    private String title;

    @Setter
    @Column(length = 1000)
    private String description;

    @Setter
    private LocalTime startTime;

    @Setter
    private LocalTime endTime;

    @Setter
    @Column(length = 255)
    private String location;

    @Setter
    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Setter
    @Column(length = 3)
    private String currency;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType type;

    public void assignItineraryDay(ItineraryDay itineraryDay) {
        this.itineraryDay = itineraryDay;
    }

    public void removeItineraryDay() {
        this.itineraryDay = null;
    }
    public void updateDetails(
            String title,
            String description,
            LocalTime startTime,
            LocalTime endTime,
            String location,
            BigDecimal estimatedCost,
            String currency,
            ActivityType type
    ) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.estimatedCost = estimatedCost;
        this.currency = currency;
        this.type = type;
    }
}