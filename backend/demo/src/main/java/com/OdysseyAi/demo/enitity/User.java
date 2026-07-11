package com.OdysseyAi.demo.enitity;

import com.OdysseyAi.demo.common.entity.BaseEntity;
import com.OdysseyAi.demo.common.enums.AccountStatus;
import com.OdysseyAi.demo.common.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void markEmailAsUnverified() {
        this.emailVerified = false;
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
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name())) ;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountStatus != AccountStatus.BLOCKED && this.accountStatus != AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }
}