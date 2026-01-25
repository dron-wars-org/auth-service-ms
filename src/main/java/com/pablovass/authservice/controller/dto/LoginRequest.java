package com.pablovass.authservice.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de login de usuario.
 */
public record LoginRequest(
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email es inválido")
    String email,

    @NotBlank(message = "La contraseña es requerida")
    String password
) {}
