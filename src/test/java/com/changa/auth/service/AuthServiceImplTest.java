package com.changa.auth.service;

import com.changa.auth.dto.AuthResponseDto;
import com.changa.auth.dto.LoginRequestDto;
import com.changa.auth.dto.RegisterRequestDto;
import com.changa.auth.service.impl.AuthServiceImpl;
import com.changa.exception.DuplicateEmailException;
import com.changa.exception.InvalidCredentialsException;
import com.changa.user.domain.entity.User;
import com.changa.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldThrowDuplicateEmailException_whenEmailAlreadyExists() {
        RegisterRequestDto request = createRegisterRequestDto();

        when(userRepository.existsByEmail("eduardo@test.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void register_shouldSaveUserWithEncodedPassword_whenEmailIsAvailable() {
        RegisterRequestDto request = createRegisterRequestDto();

        when(userRepository.existsByEmail("eduardo@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.generateToken(any(User.class)))
                .thenReturn("fake-jwt-token");

        AuthResponseDto response = authService.register(request);

        assertThat(response.name()).isEqualTo("Eduardo");
        assertThat(response.email()).isEqualTo("eduardo@test.com");

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void register_shouldReturnTokenAndUserInfo_whenEmailIsAvailable() {
        RegisterRequestDto request = createRegisterRequestDto();

        when(userRepository.existsByEmail("eduardo@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.generateToken(any(User.class)))
                .thenReturn("fake-jwt-token");

        AuthResponseDto response = authService.register(request);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.name()).isEqualTo("Eduardo");
        assertThat(response.email()).isEqualTo("eduardo@test.com");

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void login_shouldReturnTokenAndUserInfo_whenCredentialsAreValid() {
        LoginRequestDto request = createLoginRequestDto();

        User foundUser = new User(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                "eduardo@test.com",
                "encoded-password",
                "Eduardo",
                Instant.now(),
                Instant.now());

        when(userRepository.findByEmail("eduardo@test.com"))
                .thenReturn(Optional.of(foundUser));

        when(passwordEncoder.matches("password123", "encoded-password"))
                .thenReturn(true);

        when(jwtService.generateToken(foundUser))
                .thenReturn("fake-jwt-token");

        AuthResponseDto response = authService.login(request);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.userId()).isEqualTo(foundUser.getId());
        assertThat(response.name()).isEqualTo("Eduardo");
        assertThat(response.email()).isEqualTo("eduardo@test.com");

        verify(userRepository).findByEmail("eduardo@test.com");
        verify(passwordEncoder).matches("password123", "encoded-password");
        verify(jwtService).generateToken(foundUser);
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenEmailDoesNotExist() {
        LoginRequestDto request = createLoginRequestDto();

        when(userRepository.findByEmail("eduardo@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenPasswordDoesNotMatch() {
        LoginRequestDto request = createLoginRequestDto();

        User foundUser = new User(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                "eduardo@test.com",
                "encoded-password",
                "Eduardo",
                Instant.now(),
                Instant.now());

        when(userRepository.findByEmail("eduardo@test.com"))
                .thenReturn(Optional.of(foundUser));

        when(passwordEncoder.matches("password123", "encoded-password"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldNotGenerateToken_whenPasswordDoesNotMatch() {
        LoginRequestDto request = createLoginRequestDto();

        User foundUser = new User(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                "eduardo@test.com",
                "encoded-password",
                "Eduardo",
                Instant.now(),
                Instant.now());

        when(userRepository.findByEmail("eduardo@test.com"))
                .thenReturn(Optional.of(foundUser));

        when(passwordEncoder.matches("password123", "encoded-password"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(foundUser);
    }

    private RegisterRequestDto createRegisterRequestDto() {
        return new RegisterRequestDto(
                "Eduardo",
                "eduardo@test.com",
                "password123"
        );
    }

    private LoginRequestDto createLoginRequestDto() {
        return new LoginRequestDto(
                "eduardo@test.com",
                "password123"
        );
    }
}
