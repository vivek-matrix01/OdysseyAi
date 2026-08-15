package com.OdysseyAi.demo.auth.dto.response;

import com.OdysseyAi.demo.common.enums.AccountStatus;
import com.OdysseyAi.demo.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private Role role;

    private AccountStatus accountStatus;

    private boolean emailVerified;

    private int failedLoginAttempts;

    private Instant accountLockedUntil;

    private Instant createdAt;

    private Instant updatedAt;

}