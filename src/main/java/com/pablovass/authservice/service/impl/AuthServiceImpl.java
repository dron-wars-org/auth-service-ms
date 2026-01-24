package com.pablovass.authservice.service.impl;

import com.pablovass.authservice.controller.dto.RegisterRequest;
import com.pablovass.authservice.controller.mapper.UserMapper;
import com.pablovass.authservice.domain.model.entity.User;
import com.pablovass.authservice.repository.UserRepository;
import com.pablovass.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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

        userRepository.save(user);
        
        // TODO: Publicar evento Kafka UserRegistered
    }
}
