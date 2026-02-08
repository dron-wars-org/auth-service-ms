package com.pablovass.authservice.service;

import com.pablovass.authservice.controller.dto.LoginRequest;
import com.pablovass.authservice.controller.dto.LoginResponse;
import com.pablovass.authservice.controller.dto.RefreshRequest;
import com.pablovass.authservice.controller.dto.RefreshResponse;
import com.pablovass.authservice.controller.dto.RegisterRequest;
import com.pablovass.authservice.controller.dto.UserProfileResponse;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    RefreshResponse refresh(RefreshRequest request);
    UserProfileResponse getProfile(Long userId);
}
