package com.OdysseyAi.demo.controller.auth;

import com.OdysseyAi.demo.common.api.ApiResponse;
import com.OdysseyAi.demo.common.entity.RefreshToken;
import com.OdysseyAi.demo.configuration.JwtProperties;
import com.OdysseyAi.demo.dto.request.LoginRequest;
import com.OdysseyAi.demo.dto.request.RefreshTokenRequest;
import com.OdysseyAi.demo.dto.request.RegisterRequest;
import com.OdysseyAi.demo.dto.response.LoginResponse;
import com.OdysseyAi.demo.dto.response.UserResponse;
import com.OdysseyAi.demo.enitity.User;
import com.OdysseyAi.demo.service.UserService;
import com.OdysseyAi.demo.service.auth.JwtService;
import com.OdysseyAi.demo.service.auth.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse loginResponse = userService.login(request);


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
    @GetMapping("/get")
    String hello(){
        return "hello there";
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);


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