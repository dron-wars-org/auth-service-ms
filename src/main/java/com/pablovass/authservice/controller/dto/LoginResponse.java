package com.pablovass.authservice.controller.dto;

/**
 * DTO para la respuesta de login exitoso.
 * Contiene los tokens de acceso y refresh, tiempo de expiración y username.
 */
public record LoginResponse(
    String accessToken,
    String refreshToken,
    Long expiresIn,
    String username
) {}
