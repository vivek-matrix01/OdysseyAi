package com.OdysseyAi.demo.auth.integration;

import com.OdysseyAi.demo.auth.interfaces.EmailService;
import com.OdysseyAi.demo.auth.repository.PasswordResetTokenRepository;
import com.OdysseyAi.demo.auth.repository.UserRepository;

import com.OdysseyAi.demo.auth.repository.VerificationTokenRepository;
import com.OdysseyAi.demo.common.enums.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    // We mock the email service so we don't send real emails during tests
    @MockBean
    private EmailService emailService;

    @Test
    void contextLoads() {
        // If this passes, Spring Boot successfully connected to Testcontainers!
    }

    @Test
    void register_shouldCreateUser() throws Exception {
        String request = """
                {
                    "firstName": "Test",
                    "lastName": "User",
                    "email": "integration@test.com",
                    "password": "TestPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        assertTrue(userRepository.findUserByEmail("integration@test.com").isPresent());
    }

    @Test
    void register_duplicateEmail_shouldFail() throws Exception {
        // 1. Create the first user
        String request = """
                {
                    "firstName": "First",
                    "lastName": "User",
                    "email": "duplicate@test.com",
                    "password": "TestPassword123!"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        // 2. Try to register the exact same email again

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict()); // Or isConflict() depending on your Controller logic
    }

    @Test
    void register_passwordShouldBeEncodedInDatabase() throws Exception {
        String rawPassword = "SuperSecretPassword123!";
        String request = """
                {
                    "firstName": "Secure",
                    "lastName": "User",
                    "email": "secure@test.com",
                    "password": "%s"
             
                }
                """.formatted(rawPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        // Fetch the user from the actual PostgreSQL database
        var user = userRepository.findUserByEmail("secure@test.com").orElseThrow();

        // Verify the raw password is NOT saved in the database
        Assertions.assertNotEquals(rawPassword, user.getPassword());

        // Verify the saved password actually matches the raw password using Spring Security
        assertTrue(passwordEncoder.matches(rawPassword, user.getPassword()));

    }

    @Test
    void login_unverifiedUser_shouldBeRejected() throws Exception {

        String registerRequest = """
            {
                "firstName": "Login",
                "lastName": "User",
                "email": "unverified-login@test.com",
                "password": "LoginPassword123!"
            }
            """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
        ).andExpect(status().isCreated());


        String loginRequest = """
            {
                "email": "unverified-login@test.com",
                "password": "LoginPassword123!"
            }
            """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest)
        ).andExpect(status().isUnauthorized());
    }
    @Test
    void login_verifiedUser_shouldReturnTokens() throws Exception {

        String registerRequest = """
            {
                "firstName": "Login",
                "lastName": "User",
                "email": "login@test.com",
                "password": "LoginPassword123!"
            }
            """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
        ).andExpect(status().isCreated());


        var user = userRepository
                .findUserByEmail("login@test.com")
                .orElseThrow();

        user.verifyEmail();

        userRepository.save(user);


        String loginRequest = """
            {
                "email": "login@test.com",
                "password": "LoginPassword123!"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.data.accessToken").exists(),
                        jsonPath("$.data.refreshToken").exists(),
                        jsonPath("$.data.tokenType").value("Bearer")
                );
    }
    @Test
    void jwt_shouldAuthenticateProtectedEndpoint() throws Exception {

        // 1. Register user
        String registerRequest = """
            {
                "firstName": "Jwt",
                "lastName": "User",
                "email": "jwt@test.com",
                "password": "JwtPassword123!"
            }
            """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
        ).andExpect(status().isCreated());


        // 2. Verify email
        var user = userRepository
                .findUserByEmail("jwt@test.com")
                .orElseThrow();

        user.verifyEmail();

        userRepository.save(user);


        // 3. Login
        String loginRequest = """
            {
                "email": "jwt@test.com",
                "password": "JwtPassword123!"
            }
            """;

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();




        JsonNode jsonNode = objectMapper.readTree(response);

        String accessToken =
                jsonNode
                        .path("data")
                        .path("accessToken")
                        .asText();


        // 5. Use JWT against protected endpoint
        mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk());
    }
    @Test
    void protectedEndpoint_withoutToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/me")
                )
                .andExpect(status().isUnauthorized());
    }
    @Test
    void protectedEndpoint_withInvalidToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer invalid.jwt.token"
                                )
                )
                .andExpect(status().isUnauthorized());
    }
    @Test
    void protectedEndpoint_withMalformedAuthorizationHeader_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "InvalidToken"
                                )
                )
                .andExpect(status().isUnauthorized());
    }
    @Test
    void refreshToken_shouldGenerateNewTokens() throws Exception {

        // Register
        String registerRequest = """
            {
                "firstName": "Refresh",
                "lastName": "User",
                "email": "refresh@test.com",
                "password": "RefreshPassword123!"
            }
            """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
        ).andExpect(status().isCreated());


        // Verify email
        var user = userRepository
                .findUserByEmail("refresh@test.com")
                .orElseThrow();

        user.verifyEmail();
        userRepository.save(user);


        // Login
        String loginRequest = """
            {
                "email": "refresh@test.com",
                "password": "RefreshPassword123!"
            }
            """;

        String loginResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        JsonNode loginJson = objectMapper.readTree(loginResponse);

        String refreshToken = loginJson
                .path("data")
                .path("refreshToken")
                .asText();


        // Refresh
        String refreshRequest = """
            {
                "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshRequest)
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.data.accessToken").exists(),
                        jsonPath("$.data.refreshToken").exists(),
                        jsonPath("$.data.tokenType").value("Bearer")
                );
    }
    @Test
    void refreshToken_invalidToken_shouldBeRejected() throws Exception {

        String request = """
            {
                "refreshToken": "completely-invalid-refresh-token"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }
    @Test
    void verifyEmail_validToken_shouldVerifyUserAndDeleteToken() throws Exception {

        String registerRequest = """
            {
                "firstName": "Verify",
                "lastName": "User",
                "email": "verify@test.com",
                "password": "VerifyPassword123!"
            }
            """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
        ).andExpect(status().isCreated());

        var user = userRepository
                .findUserByEmail("verify@test.com")
                .orElseThrow();

        // Get the token created during registration.
        var verificationToken =
                verificationTokenRepository
                        .findByUser(user)
                        .orElseThrow();

        String token = verificationToken.getToken();

        mockMvc.perform(
                        get("/api/auth/verify-email")
                                .param("token", token)
                )
                .andExpect(status().isOk());

        // Verify user is now verified.
        var updatedUser =
                userRepository
                        .findUserByEmail("verify@test.com")
                        .orElseThrow();

        assertTrue(updatedUser.isEmailVerified());

        // Verify token was deleted.
        assertTrue(
                verificationTokenRepository
                        .findByToken(token)
                        .isEmpty()
        );
    }
    @Test
    void verifyEmail_invalidToken_shouldBeRejected() throws Exception {

        mockMvc.perform(
                        get("/api/auth/verify-email")
                                .param("token", "invalid-verification-token")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordReset_validToken_shouldChangePassword() throws Exception {

        String email = "reset@test.com";
        String oldPassword = "OldPassword123!";
        String newPassword = "NewPassword123!";

        String registerRequest = """
            {
                "firstName": "Reset",
                "lastName": "User",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, oldPassword);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
        ).andExpect(status().isCreated());


        var user = userRepository
                .findUserByEmail(email)
                .orElseThrow();

        // New password should work.
        // First verify the account.
        user.verifyEmail();
        userRepository.save(user);
        // Find the password-reset token.
        // In your actual test, you first need to trigger forgot-password.
        String forgotRequest = """
            {
                "email": "%s"
            }
            """.formatted(email);

        mockMvc.perform(
                post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forgotRequest)
        ).andExpect(status().isOk());


        var resetToken =
                passwordResetTokenRepository
                        .findByUser(user)
                        .orElseThrow();

        String token = resetToken.getToken();


        // Reset password.
        String resetRequest = """
            {
                "token": "%s",
                "newPassword": "%s"
            }
            """.formatted(token, newPassword);

        mockMvc.perform(
                post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetRequest)
        ).andExpect(status().isOk());


        // Token should be deleted.
        assertTrue(
                passwordResetTokenRepository
                        .findByToken(token)
                        .isEmpty()
        );




        String loginRequest = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, newPassword);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk());
    }
    @Test
    void passwordReset_invalidToken_shouldBeRejected() throws Exception {

        String request = """
            {
                "token": "invalid-reset-token",
                "newPassword": "NewPassword123!"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }
    @Test
    void login_afterFiveFailedAttempts_shouldLockAccount()
            throws Exception {

        String email = "locked@test.com";
        String password = "CorrectPassword123!";

        String registerRequest = """
            {
                "firstName": "Locked",
                "lastName": "User",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
        ).andExpect(status().isCreated());


        var user = userRepository
                .findUserByEmail(email)
                .orElseThrow();

        user.verifyEmail();
        userRepository.save(user);


        String wrongLogin = """
            {
                "email": "%s",
                "password": "WrongPassword123!"
            }
            """.formatted(email);


        // Five failed attempts.
        for (int i = 0; i < 5; i++) {

            mockMvc.perform(
                    post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(wrongLogin)
            );
        }


        var lockedUser =
                userRepository
                        .findUserByEmail(email)
                        .orElseThrow();

        assertFalse(lockedUser.isAccountNonLocked());
    }
    @Test
    void lockedAccount_shouldNotBeAbleToLogin()
            throws Exception {

        String email = "alreadylocked@test.com";
        String password = "CorrectPassword123!";

        String registerRequest = """
            {
                "firstName": "Locked",
                "lastName": "User",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
        ).andExpect(status().isCreated());


        var user = userRepository
                .findUserByEmail(email)
                .orElseThrow();

        user.verifyEmail();

        // Directly lock the account for the test.
        user.lockAccount(Duration.ofMinutes(15));

        userRepository.save(user);


        String loginRequest = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);


        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void user_shouldNotAccessAdminEndpoint() throws Exception {

        String email = "normaluser@test.com";
        String password = "UserPassword123!";

        String registerRequest = """
            {
                "firstName": "Normal",
                "lastName": "User",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
        ).andExpect(status().isCreated());


        var user = userRepository
                .findUserByEmail(email)
                .orElseThrow();

        user.verifyEmail();
        userRepository.save(user);


        String loginRequest = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);


        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        String accessToken = objectMapper
                .readTree(response)
                .path("data")
                .path("accessToken")
                .asText();


        mockMvc.perform(
                        get("/api/admin/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void admin_shouldAccessAdminEndpoint() throws Exception {

        String email = "admin@test.com";
        String password = "AdminPassword123!";

        String registerRequest = """
            {
                "firstName": "Admin",
                "lastName": "User",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest)
        ).andExpect(status().isCreated());


        var admin = userRepository
                .findUserByEmail(email)
                .orElseThrow();

        admin.verifyEmail();
        admin.assignRole(Role.ADMIN);

        userRepository.save(admin);


        String loginRequest = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);


        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        String accessToken = objectMapper
                .readTree(response)
                .path("data")
                .path("accessToken")
                .asText();


        mockMvc.perform(
                        get("/api/admin/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk());
    }
}