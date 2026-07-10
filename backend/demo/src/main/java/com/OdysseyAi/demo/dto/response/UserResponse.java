package com.OdysseyAi.demo.dto.response;

import com.OdysseyAi.demo.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String firstName;


    private String lastName;


    private String email;

    private Role role;

    private Long id;
}
