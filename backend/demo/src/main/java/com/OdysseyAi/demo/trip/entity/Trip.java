package com.OdysseyAi.demo.trip.entity;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.common.entity.BaseEntity;
import com.OdysseyAi.demo.common.enums.TripStatus;
import com.OdysseyAi.demo.common.exception.customexception.InvalidTripStateException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(
        name = "trips",
        indexes = {
                @Index(name = "idx_trip_user", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Trip extends BaseEntity {

    @Setter
    @Column(nullable = false, length = 100)
    private String title;

    @Setter
    @Column(length = 500)
    private String description;

    @Setter
    @Column(nullable = false, length = 100)
    private String destination;

    @Setter
    @Column(nullable = false)
    private LocalDate startDate;

    @Setter
    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)

    @Builder.Default
    private TripStatus status = TripStatus.PLANNED;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    public void assignUser(User user) {
        this.user = user;
    }
    public void start() {

        if (status != TripStatus.PLANNED) {
            throw new InvalidTripStateException(
                    "Only planned trips can be started."
            );
        }

        this.status = TripStatus.ONGOING;
    }
    public void complete() {

        if (status != TripStatus.ONGOING) {
            throw new InvalidTripStateException(
                    "Only ongoing trips can be completed."
            );
        }

        this.status = TripStatus.COMPLETED;
    }
    public void cancel() {

        if (status == TripStatus.COMPLETED) {
            throw new InvalidTripStateException(
                    "Completed trips cannot be cancelled."
            );
        }

        if (status == TripStatus.CANCELLED) {
            throw new InvalidTripStateException(
                    "Trip is already cancelled."
            );
        }

        this.status = TripStatus.CANCELLED;
    }
}
