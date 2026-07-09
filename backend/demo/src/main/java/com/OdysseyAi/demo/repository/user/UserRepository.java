package com.OdysseyAi.demo.repository.user;

import com.OdysseyAi.demo.enitity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByEmail(String email);

    boolean existsUserByEmail(String email);

    boolean existsByEmail(@NotBlank @Email @Size(max = 100) String email);
}
