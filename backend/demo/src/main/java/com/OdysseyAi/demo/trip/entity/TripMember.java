package com.OdysseyAi.demo.trip.entity;



import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.common.entity.BaseEntity;
import com.OdysseyAi.demo.common.enums.TripMemberRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "trip_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trip_member",
                        columnNames = {"trip_id", "user_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_trip_member_trip",
                        columnList = "trip_id"
                ),
                @Index(
                        name = "idx_trip_member_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "trip_id",
            nullable = false
    )
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    @Builder.Default
    private TripMemberRole role = TripMemberRole.VIEWER;

    public void changeRole(TripMemberRole role) {
        this.role = role;
    }
}