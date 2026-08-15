package com.OdysseyAi.demo.auth.controller;

import com.OdysseyAi.demo.auth.dto.request.userprofile.ChangePasswordRequest;
import com.OdysseyAi.demo.auth.dto.request.userprofile.UpdateProfileRequest;
import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.interfaces.AccountService;
import com.OdysseyAi.demo.auth.interfaces.AuthenticationFacade;
import com.OdysseyAi.demo.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationFacade authenticationFacade;

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentProfile() {
        User currentUser = authenticationFacade.getCurrentUser();

        ApiResponse<User> response = ApiResponse.<User>builder()
                .status(HttpStatus.OK.value())
                .message("User profile retrieved successfully.")
                .data(currentUser)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User currentUser = authenticationFacade.getCurrentUser();
        accountService.updateProfile(currentUser, request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Profile metadata updated successfully.")
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User currentUser = authenticationFacade.getCurrentUser();
        accountService.changePassword(currentUser, request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Password changed successfully.")
                .build();

        return ResponseEntity.ok(response);
    }
}