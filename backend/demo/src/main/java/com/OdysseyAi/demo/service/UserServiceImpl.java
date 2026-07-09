package com.OdysseyAi.demo.service;

import com.OdysseyAi.demo.common.exception.customexception.DuplicateResourceException;
import com.OdysseyAi.demo.dto.request.RegisterRequest;
import com.OdysseyAi.demo.dto.response.UserResponse;
import com.OdysseyAi.demo.enitity.User;
import com.OdysseyAi.demo.mapper.UserMapper;
import com.OdysseyAi.demo.repository.user.UserRepository;
import com.OdysseyAi.demo.repository.userservice.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email is already registered."
            );
        }

        User user = userMapper.toEntity(request);
        user.initializeNewAccount();

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }
}
