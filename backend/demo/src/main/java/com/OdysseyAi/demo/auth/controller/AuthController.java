package com.OdysseyAi.demo.auth.controller;

import com.OdysseyAi.demo.auth.dto.request.auth.ForgotPasswordRequest;
import com.OdysseyAi.demo.auth.dto.request.auth.ResetPasswordRequest;
import com.OdysseyAi.demo.auth.interfaces.AccountService;
import com.OdysseyAi.demo.common.api.ApiResponse;
import com.OdysseyAi.demo.auth.entity.RefreshToken;
import com.OdysseyAi.demo.auth.configuration.JwtProperties;
import com.OdysseyAi.demo.auth.dto.request.LoginRequest;
import com.OdysseyAi.demo.auth.dto.request.RefreshTokenRequest;
import com.OdysseyAi.demo.auth.dto.request.RegisterRequest;
import com.OdysseyAi.demo.auth.dto.response.LoginResponse;
import com.OdysseyAi.demo.auth.dto.response.UserResponse;
import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.service.interfaceImple.UserServiceImple;
import com.OdysseyAi.demo.auth.service.JwtService;
import com.OdysseyAi.demo.auth.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImple userServiceImple;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AccountService accountService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse loginResponse = userServiceImple.login(request);


        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Login successful")
                .data(loginResponse)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {

        RefreshToken verifiedToken = refreshTokenService.verify(request.getRefreshToken());
        User user = verifiedToken.getUser();


        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs())
                .build();
        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Token refreshed successfully")
                .data(loginResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal User user) {
        if (user == null) {
            ApiResponse<Void> fallback = ApiResponse.<Void>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("No active session found")
                    .build();
            return new ResponseEntity<>(fallback, HttpStatus.UNAUTHORIZED);
        }

        refreshTokenService.revoke(user);


        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully logged out")
                .data(null) // No payload needed for logout
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token) {

        accountService.verifyEmail(token);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Email verified successfully.")
                .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        accountService.initiatePasswordReset(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message(
                                "If an account exists for this email, "
                                        + "a password reset link has been sent."
                        )
                        .build()
        );
    }
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        accountService.completePasswordReset(
                request.getToken(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Password reset successfully.")
                        .build()
        );
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userServiceImple.register(request);


        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("User registered successfully.")
                .data(response)
                .build();


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }
}