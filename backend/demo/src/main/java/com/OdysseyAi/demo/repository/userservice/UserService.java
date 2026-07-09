package com.OdysseyAi.demo.repository.userservice;

import com.OdysseyAi.demo.dto.request.RegisterRequest;
import com.OdysseyAi.demo.dto.response.UserResponse;

public interface UserService {
    UserResponse register(RegisterRequest request);
}
