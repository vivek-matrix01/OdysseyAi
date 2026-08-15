package com.OdysseyAi.demo.auth.service.interfaceImple;

import com.OdysseyAi.demo.auth.configuration.PasswordResetProperties;
import com.OdysseyAi.demo.auth.configuration.StaticLinks;
import com.OdysseyAi.demo.auth.configuration.VerificationProperties;
import com.OdysseyAi.demo.auth.dto.request.admin.UpdateRoleRequest;
import com.OdysseyAi.demo.auth.dto.request.userprofile.ChangePasswordRequest;
import com.OdysseyAi.demo.auth.dto.request.userprofile.UpdateProfileRequest;
import com.OdysseyAi.demo.auth.dto.response.UserDetailResponse;
import com.OdysseyAi.demo.auth.dto.response.UserSummaryResponse;
import com.OdysseyAi.demo.auth.entity.PasswordResetToken;
import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.entity.VerificationToken;
import com.OdysseyAi.demo.auth.interfaces.AccountService;
import com.OdysseyAi.demo.auth.interfaces.EmailService;
import com.OdysseyAi.demo.auth.mapper.UserMapper;
import com.OdysseyAi.demo.auth.repository.PasswordResetTokenRepository;
import com.OdysseyAi.demo.auth.repository.UserRepository;
import com.OdysseyAi.demo.auth.repository.VerificationTokenRepository;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.common.exception.customexception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final VerificationProperties verificationProperties;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetProperties passwordResetProperties;
    private final StaticLinks staticLinks;

    @Override
    @Transactional
    public void sendVerificationEmail(User user) {

        if (user.isEmailVerified()) {
            throw new BadRequestException(
                    "Email is already verified."
            );
        }

        verificationTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(
                        Instant.now().plusMillis(
                                verificationProperties.getExpirationMs()
                        )
                )
                .build();
        System.out.println(
                "Verification expiration: "
                        + verificationProperties.getExpirationMs()
        );
        System.out.println(
                "Verification expiration: "
                        + verificationToken.getExpiresAt()
        );
        verificationTokenRepository.save(verificationToken);

        String verificationLink =
                staticLinks.getVerificationLink()
                        + token;

        emailService.sendVerificationEmail(
                user.getEmail(),
                verificationLink
        );
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Verification token is invalid or has expired."
                                ));

        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken);
            throw new BadRequestException(
                    "Verification token has expired."
            );
        }

        User user = verificationToken.getUser();

        if (user.isEmailVerified()) {
            verificationTokenRepository.delete(verificationToken);
            throw new BadRequestException(
                    "Email has already been verified."
            );
        }

        user.verifyEmail();

        verificationTokenRepository.delete(verificationToken);
    }


    @Override
    @Transactional
    public void initiatePasswordReset(String email) {

        userRepository.findUserByEmail(email)
                .ifPresent(user -> {

                    passwordResetTokenRepository.deleteByUser(user);

                    String token = UUID.randomUUID().toString();

                    PasswordResetToken resetToken =
                            PasswordResetToken.builder()
                                    .token(token)
                                    .user(user)
                                    .expiresAt(
                                            Instant.now().plusMillis(
                                                    passwordResetProperties
                                                            .getExpirationMs()
                                            )
                                    )
                                    .build();

                    passwordResetTokenRepository.save(resetToken);

                    String resetLink =
                            staticLinks.getResetPasswordLink()
                                    + token;

                    emailService.sendPasswordResetEmail(
                            user.getEmail(),
                            resetLink
                    );
                });
    }

    @Override
    @Transactional
    public void completePasswordReset(
            String token,
            String newPassword
    ) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Password reset token is invalid or has expired."
                                ));

        if (resetToken.isExpired()) {

            passwordResetTokenRepository.delete(resetToken);

            throw new BadRequestException(
                    "Password reset token has expired."
            );
        }

        User user = resetToken.getUser();

        user.updatePassword(
                passwordEncoder.encode(newPassword)
        );

        passwordResetTokenRepository.delete(resetToken);
    }

    @Transactional
    @Override
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("The current password you entered is incorrect.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BadRequestException("New password must be different from the current password.");
        }

        currentUser.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
    }

    @Transactional
    @Override
    public void updateProfile(User currentUser, UpdateProfileRequest request) {
        currentUser.setFirstName(request.getFirstName());
        currentUser.setLastName(request.getLastName());
        userRepository.save(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public  List<UserSummaryResponse>getAllUsersForAdmin() {
        List<User> users=userRepository.findAllByOrderByCreatedAtDesc();
        return userMapper.toUserSummaryResponseList(users) ;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserByIdForAdmin(Long id) {
        User user = findUserEntityById(id);
        return userMapper.toUserDetailResponse(user);
    }

    @Override
    @Transactional
    public void updateRoleByAdmin(Long id, UpdateRoleRequest request) {

        User user = findUserEntityById(id);

        if (user.getRole() == request.getRole()) {
            throw new BadRequestException("User already has the requested role.");
        }

        user.assignRole(request.getRole());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void lockUserManuallyByAdmin(Long id) {
        User user = findUserEntityById(id);
        user.block();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockUserManuallyByAdmin(Long id) {

        User user = findUserEntityById(id);
        user.unlockAccount();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUserByAdmin(Long id) {

        User user = findUserEntityById(id);
        user.markDeleted();
        userRepository.save(user);
    }

    // ==========================================
    // Private Internal Utility Helper
    // ==========================================
    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Target user record not found."));
    }
}