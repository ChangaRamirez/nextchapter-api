package com.changa.auth.service;

import com.changa.auth.dto.AuthResponseDto;
import com.changa.auth.dto.LoginRequestDto;
import com.changa.auth.dto.RegisterRequestDto;

public interface AuthService {

    AuthResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);
}
