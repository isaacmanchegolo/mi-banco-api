package com.mibanco.api.service;

import com.mibanco.api.dto.request.LoginRequest;
import com.mibanco.api.dto.request.RegisterRequest;
import com.mibanco.api.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
