package com.OdysseyAi.demo.trip.entity;

import com.OdysseyAi.demo.common.entity.BaseEntity;
import com.OdysseyAi.demo.common.enums.ItineraryStatus;
import com.OdysseyAi.demo.travel.dto.RouteData;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "itineraries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Itinerary extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "trip_id",
            nullable = false,
            unique = true
    )
    private Trip trip;

    @Setter
    @Column(length = 2000)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ItineraryStatus status =
            ItineraryStatus.DRAFT;


    /*
     * ============================================================
     * DAYS
     * ============================================================
     */
    @OneToMany(
            mappedBy = "itinerary",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("dayNumber ASC")
    @Builder.Default
    private List<ItineraryDay> days =
            new ArrayList<>();


    /*
     * ============================================================
     * COSTS
     * ============================================================
     */

    @Setter
    @Column(
            precision = 15,
            scale = 2,
            nullable = false
    )
    @Builder.Default
    private BigDecimal estimatedHotelCost =
            BigDecimal.ZERO;

    @Setter
    @Column(
            precision = 15,
            scale = 2,
            nullable = false
    )
    @Builder.Default
    private BigDecimal estimatedActivityCost =
            BigDecimal.ZERO;

    @Setter
    @Column(
            precision = 15,
            scale = 2,
            nullable = false
    )
    @Builder.Default
    private BigDecimal estimatedTransportCost =
            BigDecimal.ZERO;

    @Setter
    @Column(
            precision = 15,
            scale = 2,
            nullable = false
    )
    @Builder.Default
    private BigDecimal estimatedTotalCost =
            BigDecimal.ZERO;


    /*
     * ============================================================
     * ROUTES
     * ============================================================
     *
     * Stored as JSON text in the database.
     *
     * Example:
     *
     * [
     *   {
     *     "origin": "Hotel",
     *     "destination": "Place A",
     *     "travelMode": "DRIVE",
     *     "durationSeconds": 1200,
     *     "distanceMeters": 5000,
     *     "fare": null,
     *     "fareCurrency": null
     *   }
     * ]
     */
    @Setter
    @Convert(
            converter = RouteDataListConverter.class
    )
    @Column(
            name = "routes_json",
            columnDefinition = "TEXT"
    )
    @Builder.Default
    private List<RouteData> routes =
            new ArrayList<>();


    /*
     * ============================================================
     * RELATIONSHIP METHODS
     * ============================================================
     */

    public void assignTrip(
            Trip trip
    ) {

        this.trip = trip;
    }

    public void addDay(
            ItineraryDay day
    ) {

        days.add(day);

        day.assignItinerary(
                this
        );
    }

    public void removeDay(
            ItineraryDay day
    ) {

        days.remove(day);

        day.removeItinerary();
    }

    public void replaceDays(
            List<ItineraryDay> days
    ) {

        this.days.clear();

        if (days != null) {

            this.days.addAll(days);
        }
    }

    public void clearDays() {

        days.clear();
    }


    /*
     * ============================================================
     * COST UPDATE
     * ============================================================
     */

    public void updateCostBreakdown(
            BigDecimal hotelCost,
            BigDecimal activityCost,
            BigDecimal transportCost,
            BigDecimal totalCost
    ) {

        this.estimatedHotelCost =
                safeAmount(hotelCost);

        this.estimatedActivityCost =
                safeAmount(activityCost);

        this.estimatedTransportCost =
                safeAmount(transportCost);

        this.estimatedTotalCost =
                safeAmount(totalCost);
    }


    /*
     * ============================================================
     * ROUTE UPDATE
     * ============================================================
     */

    public void updateRoutes(
            List<RouteData> routes
    ) {

        this.routes =
                routes == null
                        ? new ArrayList<>()
                        : new ArrayList<>(routes);
    }


    /*
     * ============================================================
     * ITINERARY STATE
     * ============================================================
     */

    public void updateSummary(
            String summary
    ) {

        this.summary = summary;
    }

    public void markGenerated() {

        this.status =
                ItineraryStatus.GENERATED;
    }

    public void markModified() {

        this.status =
                ItineraryStatus.MODIFIED;
    }


    private BigDecimal safeAmount(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}