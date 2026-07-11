package com.OdysseyAi.demo.dto.request;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token string cannot be empty")
    private String refreshToken;
}