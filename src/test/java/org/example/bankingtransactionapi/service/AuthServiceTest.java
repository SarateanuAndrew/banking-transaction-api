package org.example.bankingtransactionapi.service;

import org.example.bankingtransactionapi.config.JwtService;
import org.example.bankingtransactionapi.dto.request.LoginRequest;
import org.example.bankingtransactionapi.dto.request.RegisterRequest;
import org.example.bankingtransactionapi.dto.response.AuthResponse;
import org.example.bankingtransactionapi.exception.DuplicateResourceException;
import org.example.bankingtransactionapi.model.User;
import org.example.bankingtransactionapi.model.enums.Role;
import org.example.bankingtransactionapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    @Test
    void register_ShouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WhenUsernameTaken_ShouldThrowDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("john");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenEmailTaken_ShouldThrowDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("john@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .role(Role.ROLE_USER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("john");
    }

    @Test
    void login_WhenUserNotFound_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class);
    }
}
