package com.OdysseyAi.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;

    private String tokenType;

    private long expiresIn;
}
