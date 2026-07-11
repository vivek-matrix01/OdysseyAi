package com.OdysseyAi.demo.repository.refreshtoken;

import com.OdysseyAi.demo.common.entity.RefreshToken;
import com.OdysseyAi.demo.enitity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}