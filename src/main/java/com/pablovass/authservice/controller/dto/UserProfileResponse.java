package com.pablovass.authservice.controller.dto;

import java.time.LocalDateTime;

/**
 * Response DTO para consulta de perfil de usuario.
 * HU-AUTH-04: Perfil Autenticado
 */
public record UserProfileResponse(
    Long id,
    String username,
    String email,
    LocalDateTime createdAt
) {}
