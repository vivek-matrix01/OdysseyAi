package com.OdysseyAi.demo.enitity;

import com.OdysseyAi.demo.common.entity.BaseEntity;
import com.OdysseyAi.demo.common.enums.AccountStatus;
import com.OdysseyAi.demo.common.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name="users")
@Entity
public class User extends BaseEntity {


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
}