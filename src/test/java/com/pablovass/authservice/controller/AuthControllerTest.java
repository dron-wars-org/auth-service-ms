package com.pablovass.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablovass.authservice.controller.advice.GlobalExceptionHandler;
import com.pablovass.authservice.controller.dto.RegisterRequest;
import com.pablovass.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Controller Unit Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 201 Created when request is valid")
    void register_ShouldReturn201_WhenRequestIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "testuser", "Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request is invalid")
    void register_ShouldReturn400_WhenRequestIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("invalid-email", "us", "123"); // Invalid data

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 Conflict when user already exists")
    void register_ShouldReturn409_WhenUserExists() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "testuser", "Password123");
        
        doThrow(new IllegalArgumentException("El email ya está registrado"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El email ya está registrado"));
    }
}
