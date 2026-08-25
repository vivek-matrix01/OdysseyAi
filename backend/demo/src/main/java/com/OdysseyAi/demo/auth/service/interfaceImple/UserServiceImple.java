package com.OdysseyAi.demo.auth.service.interfaceImple;

import com.OdysseyAi.demo.auth.configuration.JwtProperties;
import com.OdysseyAi.demo.auth.dto.request.LoginRequest;
import com.OdysseyAi.demo.auth.dto.request.RegisterRequest;
import com.OdysseyAi.demo.auth.dto.response.LoginResponse;
import com.OdysseyAi.demo.auth.dto.response.UserResponse;
import com.OdysseyAi.demo.auth.entity.RefreshToken;
import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.interfaces.AccountService;
import com.OdysseyAi.demo.auth.interfaces.UserService;
import com.OdysseyAi.demo.auth.mapper.UserMapper;
import com.OdysseyAi.demo.auth.repository.UserRepository;
import com.OdysseyAi.demo.auth.service.JwtService;
import com.OdysseyAi.demo.auth.service.RefreshTokenService;
import com.OdysseyAi.demo.common.exception.customexception.DuplicateResourceException;
import com.OdysseyAi.demo.common.exception.customexception.InvalidCredentialsException;
import com.OdysseyAi.demo.common.exception.customexception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImple implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private  final JwtService jwtService;
    private  final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration ACCOUNT_LOCK_DURATION = Duration.ofMinutes(15);
    private  final AccountService accountService;
    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public LoginResponse login(LoginRequest request) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )

            );


            User user = (User) authentication.getPrincipal();
            if(!user.isEmailVerified()) throw new UnauthorizedException("Email not verified");

            user.resetFailedAttempts();
            userRepository.save(user);


            String accessToken = jwtService.generateAccessToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtProperties.getAccessTokenExpirationMs())
                    .build();
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            //  The account WAS unlocked, but the password was wrong!
            // This is exactly where we intercept the failure to increment the counter in the DB.
            handleFailedLogin(request.getEmail());

            throw new com.OdysseyAi.demo.common.exception.customexception.InvalidCredentialsException(
                    "Invalid email or password credentials."
            );
        } catch (org.springframework.security.authentication.LockedException ex) {
            // The account is ALREADY locked out in the DB.:
            // Spring Security blocked them immediately at Step 1. No need to increment the counter
            // because they are already serving their 15-minute cooldown time.
            throw new com.OdysseyAi.demo.common.exception.customexception.ForbiddenException(
                    "This account is temporarily locked due to multiple failed login attempts. Try again later."
            );
        }

    }
    private void handleFailedLogin(String email) {
        userRepository.findUserByEmail(email).ifPresent(user -> {
            user.incrementFailedAttempts();


            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lockAccount(ACCOUNT_LOCK_DURATION);
            }

            userRepository.save(user);
        });
    }

@Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email is already registered."
            );
        }

        User user = userMapper.toEntity(request);
        user.initializeNewAccount();

        user.updatePassword(passwordEncoder.encode(request.getPassword()));



        User savedUser = userRepository.save(user);
        accountService.sendVerificationEmail(savedUser);
        return userMapper.toResponse(savedUser);
    }


}
