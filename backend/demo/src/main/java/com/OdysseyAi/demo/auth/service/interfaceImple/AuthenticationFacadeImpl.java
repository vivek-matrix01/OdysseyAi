package com.OdysseyAi.demo.auth.service.interfaceImple;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.repository.UserRepository;
import com.OdysseyAi.demo.auth.interfaces.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UsernameNotFoundException("No authenticated user found in context");
        }

        String email = authentication.getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}