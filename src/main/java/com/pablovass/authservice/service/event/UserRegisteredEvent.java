package com.pablovass.authservice.service.event;

import java.time.LocalDateTime;

/**
 * Evento que se publica cuando un usuario se registra exitosamente.
 */
public record UserRegisteredEvent(
    Long userId,
    String username,
    String email,
    LocalDateTime timestamp
) {}
