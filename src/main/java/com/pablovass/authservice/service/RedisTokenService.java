package com.pablovass.authservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para gestión de refresh tokens en Redis.
 * Almacena tokens con TTL de 7 días.
 */
@Service
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public RedisTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Genera y almacena un refresh token en Redis.
     *
     * @param userId ID del usuario
     * @return Refresh token generado (UUID)
     */
    public String generateAndStoreRefreshToken(Long userId) {
        String refreshToken = UUID.randomUUID().toString();
        String key = "refresh_token:" + userId;
        
        // Almacenar en Redis con TTL
        redisTemplate.opsForValue().set(
            key,
            refreshToken,
            refreshTokenExpiration,
            TimeUnit.MILLISECONDS
        );
        
        return refreshToken;
    }

    /**
     * Valida un refresh token verificando que exista en Redis y coincida.
     *
     * @param userId ID del usuario
     * @param refreshToken Token a validar
     * @return true si el token es válido
     */
    public Boolean validateRefreshToken(Long userId, String refreshToken) {
        String key = "refresh_token:" + userId;
        String storedToken = redisTemplate.opsForValue().get(key);
        return refreshToken.equals(storedToken);
    }

    /**
     * Elimina un refresh token de Redis (logout).
     *
     * @param userId ID del usuario
     */
    public void revokeRefreshToken(Long userId) {
        String key = "refresh_token:" + userId;
        redisTemplate.delete(key);
    }
}
