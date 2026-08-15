package com.OdysseyAi.demo.auth.service;


import com.OdysseyAi.demo.auth.entity.RefreshToken;
import com.OdysseyAi.demo.common.exception.customexception.TokenRefreshException;
import com.OdysseyAi.demo.auth.configuration.JwtProperties;
import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public RefreshToken createRefreshToken(User user) {

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verify(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found in system"));

        if (refreshToken.isRevoked()) {
            throw new TokenRefreshException("This refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            // Lazy delete expired tokens
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token has expired. Please log in again");
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}