package com.OdysseyAi.demo.auth.repository;

import com.OdysseyAi.demo.auth.entity.PasswordResetToken;
import com.OdysseyAi.demo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByUser(User user);
}
