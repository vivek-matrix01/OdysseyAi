package com.OdysseyAi.demo.auth.entity;

import com.OdysseyAi.demo.common.entity.BaseEntity;
import com.OdysseyAi.demo.common.enums.AccountStatus;
import com.OdysseyAi.demo.common.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Table(name="users")
@Entity
public class User extends BaseEntity implements UserDetails {


    @Setter
    @NotBlank
    @Column(nullable = false, length = 50)
    private String firstName;

    @Setter
    @NotBlank
    @Column(nullable = false, length = 50)
    private String lastName;


    @Email
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private boolean emailVerified;

    @Column(nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    private java.time.Instant accountLockedUntil;

    //utility methods
    public void initializeNewAccount() {
        this.role = Role.USER;
        this.accountStatus = AccountStatus.ACTIVE;
        this.emailVerified = false;

    }

    public void assignRole(Role role) {
        this.role = role;
    }

    public void activate() {
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void suspend() {
        this.accountStatus = AccountStatus.SUSPENDED;
    }

    public void block() {
        this.accountStatus = AccountStatus.BLOCKED;
    }
    public void markDeleted() {
        this.accountStatus = AccountStatus.DELETED;
    }



    public void markEmailAsUnverified() {
        this.emailVerified = false;
    }
    public void verifyEmail(){
        this.emailVerified = true;
    }

    public void updatePassword(String encodedPassword) {

        this.password = encodedPassword;
    }


    public void changeEmail(String newEmail) {
        this.email = newEmail;
        this.emailVerified = false;
    }


    // Implementing user details for spring security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (this.accountLockedUntil != null && this.accountLockedUntil.isBefore(java.time.Instant.now())) {
            this.accountLockedUntil = null;
            this.failedLoginAttempts = 0;
            this.accountStatus = AccountStatus.ACTIVE;
            return true;
        }
        return this.accountStatus != AccountStatus.BLOCKED &&
                this.accountStatus != AccountStatus.SUSPENDED;
    }



    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }


        // methods for preventing multiple attempts

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void lockAccount(java.time.Duration duration) {
        this.accountLockedUntil = java.time.Instant.now().plus(duration);
        this.accountStatus = AccountStatus.BLOCKED;
    }

    public void unlockAccount() {
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
        this.accountStatus = AccountStatus.ACTIVE;
    }


}