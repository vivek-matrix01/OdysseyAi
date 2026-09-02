package com.OdysseyAi.demo.trip.entity;

import com.OdysseyAi.demo.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "itinerary_days",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_itinerary_day_number",
                        columnNames = {"itinerary_id", "day_number"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ItineraryDay extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "itinerary_id",
            nullable = false
    )
    private Itinerary itinerary;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Setter
    @Column(nullable = false)
    private LocalDate date;

    @Setter
    @Column(length = 1000)
    private String notes;

    @OneToMany(
            mappedBy = "itineraryDay",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("startTime ASC")
    @Builder.Default
    private List<Activity> activities = new ArrayList<>();

    public void assignItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }

    public void removeItinerary() {
        this.itinerary = null;
    }

    public void assignDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    public void updateDate(LocalDate date) {
        this.date = date;
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
        activity.assignItineraryDay(this);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
        activity.removeItineraryDay();
    }
}