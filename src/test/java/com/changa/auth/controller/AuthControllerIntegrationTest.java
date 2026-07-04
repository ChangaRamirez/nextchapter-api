package com.changa.auth.controller;

import com.changa.auth.dto.LoginRequestDto;
import com.changa.auth.dto.RegisterRequestDto;
import com.changa.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldReturnCreatedUserAndToken_whenRequestIsValid() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "Eduardo",
                "eduardo@test.com",
                "password123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.name").value("Eduardo"))
            .andExpect(jsonPath("$.email").value("eduardo@test.com"))
            .andExpect(jsonPath("$.token").exists());

    }

    @Test
    void register_shouldThrowDuplicateEmailException_whenEmailIsAlreadyUsed() throws Exception {

        RegisterRequestDto request = new RegisterRequestDto(
                "Eduardo",
                "eduardo@test.com",
                "password123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

    }

    @Test
    void login_shouldReturnTokenAndUserInfo_whenCredentialsAreValid() throws Exception {

        RegisterRequestDto registerRequest = new RegisterRequestDto(
                "Eduardo",
                "eduardo@test.com",
                "password123"
        );

        LoginRequestDto loginRequest = new LoginRequestDto(
                "eduardo@test.com",
                "password123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.name").value("Eduardo"))
                .andExpect(jsonPath("$.email").value(loginRequest.email()));
    }

    @Test
    void login_shouldReturnUnauthorized_whenPasswordIsIncorrect() throws Exception {

       RegisterRequestDto registerRequest = new RegisterRequestDto(
                "Eduardo",
                "eduardo@test.com",
                "password123"
        );

        LoginRequestDto loginRequest = new LoginRequestDto(
                "eduardo@test.com",
                "password321"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
   }

   @Test
    void register_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception{

        RegisterRequestDto request = new RegisterRequestDto(
                "",
                "not-an-email",
                ""
        );

       mockMvc.perform(post("/api/v1/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.error").exists());

   }
}
