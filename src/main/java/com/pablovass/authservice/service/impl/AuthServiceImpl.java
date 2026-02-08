package com.pablovass.authservice.service.impl;

import com.pablovass.authservice.controller.dto.LoginRequest;
import com.pablovass.authservice.controller.dto.LoginResponse;
import com.pablovass.authservice.controller.dto.GoogleLoginRequest;
import com.pablovass.authservice.controller.dto.RefreshRequest;
import com.pablovass.authservice.controller.dto.RefreshResponse;
import com.pablovass.authservice.controller.dto.RegisterRequest;
import com.pablovass.authservice.controller.dto.UserProfileResponse;
import com.pablovass.authservice.controller.mapper.UserMapper;
import com.pablovass.authservice.domain.model.entity.User;
import com.pablovass.authservice.repository.UserRepository;
import com.pablovass.authservice.service.AuthService;
import com.pablovass.authservice.service.JwtService;
import com.pablovass.authservice.service.RedisTokenService;
import com.pablovass.authservice.service.event.UserLoggedInEvent;
import com.pablovass.authservice.service.event.UserRegisteredEvent;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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

    @Value("${google.client-id}")
    private String googleClientId;

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
        user.setProvider("LOCAL");

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

        if (!"LOCAL".equals(user.getProvider())) {
            throw new BadCredentialsException("Por favor use " + user.getProvider() + " para iniciar sesión");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return createLoginResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse googleLogin(GoogleLoginRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.idToken());
            if (idToken == null) {
                throw new BadCredentialsException("Token de Google inválido");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            
            // Buscar usuario o crear uno nuevo
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> registerGoogleUser(email, name));

            if (!"GOOGLE".equals(user.getProvider())) {
                throw new BadCredentialsException("Este email ya está registrado con otro método");
            }

            return createLoginResponse(user);

        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error en Google Login: {}", e.getMessage());
            throw new BadCredentialsException("Fallo en la autenticación con Google");
        }
    }

    private User registerGoogleUser(String email, String name) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int count = 1;
        
        // Evitar colisiones de username
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + count++;
        }

        User user = User.builder()
                .email(email)
                .username(username)
                .password("") // No password for social login
                .provider("GOOGLE")
                .roles(Collections.singleton("ROLE_USER"))
                .build();

        User savedUser = userRepository.save(user);

        publishEvent(new UserRegisteredEvent(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            LocalDateTime.now()
        ));

        return savedUser;
    }

    private LoginResponse createLoginResponse(User user) {
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

    @Override
    @Transactional(readOnly = true)
    public RefreshResponse refresh(RefreshRequest request) {
        // Obtener userId desde el refresh token
        Long userId = redisTokenService.getUserIdFromRefreshToken(request.refreshToken());
        
        if (userId == null) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        // Validar que el token coincida con el almacenado
        if (!redisTokenService.validateRefreshToken(userId, request.refreshToken())) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        // Buscar usuario en la base de datos
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        // Generar nuevo Access Token
        String role = user.getRoles().stream().findFirst().orElse("ROLE_USER");
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), role);

        return new RefreshResponse(newAccessToken, accessTokenExpiration);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt()
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
