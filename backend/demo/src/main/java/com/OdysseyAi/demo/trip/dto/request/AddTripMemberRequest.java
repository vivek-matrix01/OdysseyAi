package com.OdysseyAi.demo.trip.dto.request;

import com.OdysseyAi.demo.common.enums.TripMemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddTripMemberRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull
    private TripMemberRole role;
}