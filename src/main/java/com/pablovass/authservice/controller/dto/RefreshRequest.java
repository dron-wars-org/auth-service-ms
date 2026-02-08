package com.pablovass.authservice.controller.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO para renovación de Access Token.
 * HU-AUTH-03: Refresh Token
 */
public record RefreshRequest(
    @NotBlank(message = "El refresh token es obligatorio")
    String refreshToken
) {}
