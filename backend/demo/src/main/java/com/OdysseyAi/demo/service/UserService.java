package com.OdysseyAi.demo.service;

import com.OdysseyAi.demo.common.entity.RefreshToken;
import com.OdysseyAi.demo.common.exception.customexception.DuplicateResourceException;
import com.OdysseyAi.demo.configuration.JwtProperties;
import com.OdysseyAi.demo.dto.request.LoginRequest;
import com.OdysseyAi.demo.dto.request.RegisterRequest;
import com.OdysseyAi.demo.dto.response.LoginResponse;
import com.OdysseyAi.demo.dto.response.UserResponse;
import com.OdysseyAi.demo.enitity.User;
import com.OdysseyAi.demo.mapper.UserMapper;
import com.OdysseyAi.demo.repository.user.UserRepository;

import com.OdysseyAi.demo.service.auth.JwtService;
import com.OdysseyAi.demo.service.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService  {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private  final JwtService jwtService;
    private  final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse login(LoginRequest request) {

        Authentication authentication=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(user);


        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs())
                .build();
    }


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

        return userMapper.toResponse(savedUser);
    }
}
