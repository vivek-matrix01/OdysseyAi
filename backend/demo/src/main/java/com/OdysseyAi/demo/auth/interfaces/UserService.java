package com.OdysseyAi.demo.auth.interfaces;

import com.OdysseyAi.demo.auth.dto.request.LoginRequest;
import com.OdysseyAi.demo.auth.dto.request.RegisterRequest;
import com.OdysseyAi.demo.auth.dto.response.LoginResponse;
import com.OdysseyAi.demo.auth.dto.response.UserResponse;

public interface UserService {
    public LoginResponse login(LoginRequest request) ;

    public UserResponse register(RegisterRequest request);
}
