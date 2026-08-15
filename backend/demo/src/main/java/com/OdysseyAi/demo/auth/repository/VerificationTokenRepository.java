package com.OdysseyAi.demo.auth.repository;



import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

    void deleteByUser(User user);
}