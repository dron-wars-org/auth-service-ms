package com.pablovass.authservice.service.impl;

import com.pablovass.authservice.controller.dto.LoginRequest;
import com.pablovass.authservice.controller.dto.LoginResponse;
import com.pablovass.authservice.controller.dto.RegisterRequest;
import com.pablovass.authservice.controller.mapper.UserMapper;
import com.pablovass.authservice.domain.model.entity.User;
import com.pablovass.authservice.repository.UserRepository;
import com.pablovass.authservice.service.AuthService;
import com.pablovass.authservice.service.JwtService;
import com.pablovass.authservice.service.RedisTokenService;
import com.pablovass.authservice.service.event.UserLoggedInEvent;
import com.pablovass.authservice.service.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.user-events}")
    private String userEventsTopic;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Collections.singleton("ROLE_USER"));

        User savedUser = userRepository.save(user);
        
        // Publicar evento Kafka UserRegistered
        publishEvent(new UserRegisteredEvent(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            LocalDateTime.now()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // Generar Access Token
        String role = user.getRoles().stream().findFirst().orElse("ROLE_USER");
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), role);

        // Generar y guardar Refresh Token en Redis
        String refreshToken = redisTokenService.generateAndStoreRefreshToken(user.getId());

        // Publicar evento Kafka UserLoggedIn
        publishEvent(new UserLoggedInEvent(
            user.getId(),
            user.getUsername(),
            LocalDateTime.now()
        ));

        return new LoginResponse(
            accessToken,
            refreshToken,
            accessTokenExpiration,
            user.getUsername()
        );
    }

    private void publishEvent(Object event) {
        try {
            kafkaTemplate.send(userEventsTopic, event);
        } catch (Exception e) {
            log.error("Error al publicar evento en Kafka: {}", e.getMessage());
            // No bloqueamos el flujo principal si Kafka falla (Edge Case de la HU)
        }
    }
}
