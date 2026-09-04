package com.OdysseyAi.demo.auth.configuration;

import com.OdysseyAi.demo.auth.entity.User;


import com.OdysseyAi.demo.auth.repository.UserRepository;
import com.OdysseyAi.demo.common.enums.AccountStatus;
import com.OdysseyAi.demo.common.enums.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataIntializer{
    @Bean
    public CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("vivekaithani01@gmail.com")) {
                User admin = User.builder().firstName("VIVEK").lastName("AITHANI")
                        .role(Role.ADMIN).emailVerified(true).email("vivekaithani01@gmail.com")
                        .password(passwordEncoder.encode("vivekaithani01@gmail.com"))
                        .accountStatus(AccountStatus.ACTIVE).build();
                userRepository.save(admin);

                //user
                User user = User.builder().firstName("john").lastName("john")
                        .role(Role.USER).emailVerified(true).email("john@test.com")
                        .password(passwordEncoder.encode("john@test.com"))
                        .accountStatus(AccountStatus.ACTIVE).build();
                userRepository.save(user);
            }

        };
    }
}
