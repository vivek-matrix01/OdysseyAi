package com.OdysseyAi.demo.auth.interfaces;

import com.OdysseyAi.demo.auth.dto.request.admin.UpdateRoleRequest;
import com.OdysseyAi.demo.auth.dto.request.userprofile.ChangePasswordRequest;
import com.OdysseyAi.demo.auth.dto.request.userprofile.UpdateProfileRequest;
import com.OdysseyAi.demo.auth.dto.response.UserDetailResponse;
import com.OdysseyAi.demo.auth.dto.response.UserSummaryResponse;
import com.OdysseyAi.demo.auth.entity.User;

import java.util.List;

public interface AccountService{

    // ==========================================
    // Public & Self-Service Operations
    // ==========================================
    void sendVerificationEmail(User user);
    void verifyEmail(String token);
    void initiatePasswordReset(String email);
    void completePasswordReset(String token, String newPassword);
    void changePassword(User currentUser, ChangePasswordRequest request);
    void updateProfile(User currentUser, UpdateProfileRequest request);

    // ==========================================
    // Administrative Operations
    // ==========================================
    List<UserSummaryResponse> getAllUsersForAdmin();
    UserDetailResponse getUserByIdForAdmin(Long id);
    void updateRoleByAdmin(Long id, UpdateRoleRequest request);
    void lockUserManuallyByAdmin(Long id);
    void unlockUserManuallyByAdmin(Long id);
    void deleteUserByAdmin(Long id);
}
