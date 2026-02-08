package com.pablovass.authservice.controller.dto;

/**
 * Response DTO para renovación de Access Token.
 * HU-AUTH-03: Refresh Token
 */
public record RefreshResponse(
    String accessToken,
    Long expiresIn
) {}
