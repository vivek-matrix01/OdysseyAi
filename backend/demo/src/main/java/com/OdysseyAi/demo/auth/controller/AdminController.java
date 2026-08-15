package com.OdysseyAi.demo.auth.controller;


import com.OdysseyAi.demo.auth.dto.request.admin.UpdateRoleRequest;
import com.OdysseyAi.demo.auth.dto.response.UserDetailResponse;
import com.OdysseyAi.demo.auth.dto.response.UserSummaryResponse;
import com.OdysseyAi.demo.auth.interfaces.AccountService;
import com.OdysseyAi.demo.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllUsers() {
        List<UserSummaryResponse> users = accountService.getAllUsersForAdmin();
        ApiResponse<List<UserSummaryResponse>> response = ApiResponse.<List<UserSummaryResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("All user accounts retrieved successfully.")
                .data(users)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserById(@PathVariable Long id) {
        UserDetailResponse userDetailResponse = accountService.getUserByIdForAdmin(id);
        ApiResponse<UserDetailResponse> response = ApiResponse.<UserDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("User account details retrieved successfully.")
                .data(userDetailResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Void>> changeUserRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        accountService.updateRoleByAdmin(id, request);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("User account authorization role updated successfully.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUserAccount(@PathVariable Long id) {
        accountService.lockUserManuallyByAdmin(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("User account has been locked and suspended manually.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUserAccount(@PathVariable Long id) {
        accountService.unlockUserManuallyByAdmin(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("User account tracking state cleared and restored to active.")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUserAccount(@PathVariable Long id) {
        accountService.deleteUserByAdmin(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("User account has been permanently purged from the system databases.")
                .build();
        return ResponseEntity.ok(response);
    }
}