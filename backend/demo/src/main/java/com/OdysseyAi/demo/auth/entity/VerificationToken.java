package com.OdysseyAi.demo.auth.entity;

import com.OdysseyAi.demo.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "verification_tokens",
        indexes = {
                @Index(name = "idx_verification_token", columnList = "token")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken extends BaseEntity {

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}