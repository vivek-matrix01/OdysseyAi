package com.OdysseyAi.demo.user;

import com.OdysseyAi.demo.enitity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByEmail(String email);

    boolean existsUserByEmail(String email);
}
