package com.pablovass.authservice.service;

import com.pablovass.authservice.controller.dto.LoginRequest;
import com.pablovass.authservice.controller.dto.LoginResponse;
import com.pablovass.authservice.controller.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
