package com.OdysseyAi.demo.auth.repository;

import com.OdysseyAi.demo.auth.entity.RefreshToken;
import com.OdysseyAi.demo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}