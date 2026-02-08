package com.pablovass.authservice.controller.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO para login con Google.
 * HU-AUTH-05: Integración OAuth2
 */
public record GoogleLoginRequest(
    @NotBlank(message = "El ID Token es obligatorio")
    String idToken
) {}
