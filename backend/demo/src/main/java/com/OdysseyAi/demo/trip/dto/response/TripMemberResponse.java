package com.OdysseyAi.demo.trip.dto.response;


import com.OdysseyAi.demo.common.enums.TripMemberRole;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripMemberResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private TripMemberRole role;
}