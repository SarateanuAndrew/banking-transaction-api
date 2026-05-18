package org.example.bankingtransactionapi.service;

import lombok.RequiredArgsConstructor;
import org.example.bankingtransactionapi.config.JwtService;
import org.example.bankingtransactionapi.dto.request.LoginRequest;
import org.example.bankingtransactionapi.dto.request.RegisterRequest;
import org.example.bankingtransactionapi.dto.response.AuthResponse;
import org.example.bankingtransactionapi.exception.DuplicateResourceException;
import org.example.bankingtransactionapi.model.User;
import org.example.bankingtransactionapi.model.enums.Role;
import org.example.bankingtransactionapi.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
