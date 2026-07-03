package com.OdysseyAi.demo.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(updatable = false)
    private Long id;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(nullable = false,updatable = false)
    private Instant createdAt;

    @Setter(AccessLevel.NONE)
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

}
