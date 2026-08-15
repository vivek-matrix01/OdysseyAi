package com.OdysseyAi.demo.auth.dto.response;

import com.OdysseyAi.demo.common.enums.AccountStatus;
import com.OdysseyAi.demo.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private Role role;

    private AccountStatus accountStatus;

}