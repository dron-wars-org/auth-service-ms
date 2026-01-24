package com.pablovass.authservice.service.event;

import java.time.LocalDateTime;

/**
 * Evento que se publica cuando un usuario inicia sesión exitosamente.
 */
public record UserLoggedInEvent(
    Long userId,
    String username,
    LocalDateTime timestamp
) {}
