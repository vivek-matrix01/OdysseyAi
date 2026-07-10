package com.OdysseyAi.demo.controller.auth;

import com.OdysseyAi.demo.common.api.ApiResponse;
import com.OdysseyAi.demo.dto.request.RegisterRequest;
import com.OdysseyAi.demo.dto.response.UserResponse;
import com.OdysseyAi.demo.repository.userservice.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;


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