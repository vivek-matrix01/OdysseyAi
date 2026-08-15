package com.OdysseyAi.demo.auth.dto.request.admin;

import com.OdysseyAi.demo.common.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {
    @NotNull(message = "Target role cannot be null")
    private Role role;
}