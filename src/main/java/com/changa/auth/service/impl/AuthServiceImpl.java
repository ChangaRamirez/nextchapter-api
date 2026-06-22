package com.changa.auth.service.impl;

import com.changa.auth.dto.AuthResponseDto;
import com.changa.auth.dto.LoginRequestDto;
import com.changa.auth.dto.RegisterRequestDto;
import com.changa.auth.service.AuthService;
import com.changa.auth.service.JwtService;
import com.changa.exception.DuplicateEmailException;
import com.changa.exception.InvalidCredentialsException;
import com.changa.user.domain.entity.User;
import com.changa.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Instant now = Instant.now();

        User newUser = new User(
                null,
                request.email(),
                encodedPassword,
                request.name(),
                now,
                now
        );

        User createdUser = userRepository.save(newUser);

        String token = jwtService.generateToken(createdUser);

        return new AuthResponseDto(token, createdUser.getId(), createdUser.getName(), createdUser.getEmail());
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {

        User existingUser = userRepository.findByEmail(request.email()).orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), existingUser.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(existingUser);

        return new AuthResponseDto(
                token,
                existingUser.getId(),
                existingUser.getName(),
                existingUser.getEmail()
        );
    }
}
