package com.OdysseyAi.demo.auth.repository;

import com.OdysseyAi.demo.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByEmail(String email);

    boolean existsByEmail(@NotBlank @Email @Size(max = 100) String email);

    List<User> findAllByOrderByCreatedAtDesc();

}
