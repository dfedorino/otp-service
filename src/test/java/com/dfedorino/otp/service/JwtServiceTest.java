package com.dfedorino.otp.service;

import com.dfedorino.otp.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "this_is_a_very_long_secret_key_for_jwt_hmac_256";
    private static final long EXPIRATION_SECONDS = 3600;
    
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_SECONDS);
    }

    @Test
    void should_generate_valid_token() {
        // Act
        String token = jwtService.generateToken(
            1L,
            "john",
            Role.USER
        );

        // Assert
        assertThat(token)
            .isNotBlank();
        
        assertThat(jwtService.isTokenValid(token))
            .isTrue();
    }

    @Test
    void should_extract_username_from_token() {
        // Arrange
        String token = jwtService.generateToken(
            1L,
            "john",
            Role.USER
        );

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertThat(username)
            .isEqualTo("john");
    }

    @Test
    void should_extract_role_from_token() {
        // Arrange
        String token = jwtService.generateToken(
            1L,
            "john",
            Role.ADMIN
        );

        // Act
        Role role = jwtService.extractRole(token);

        // Assert
        assertThat(role)
            .isEqualTo(Role.ADMIN);
    }

    @Test
    void should_extract_user_id_from_token() {
        // Arrange
        String token = jwtService.generateToken(
            42L,
            "john",
            Role.USER
        );

        // Act
        long userId = jwtService.extractUserId(token);

        // Assert
        assertThat(userId)
            .isEqualTo(42L);
    }

    @Test
    void should_return_false_for_expired_token() throws InterruptedException {
        // Arrange
        JwtService shortExpirationJwtService = new JwtService(SECRET, 1);
        String token = shortExpirationJwtService.generateToken(
            1L,
            "john",
            Role.USER
        );

        // Sleep to expire the token
        Thread.sleep(2000);

        // Act
        boolean valid = shortExpirationJwtService.isTokenValid(token);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    void should_return_false_for_invalid_signature() {
        // Arrange
        String token = jwtService.generateToken(
            1L,
            "john",
            Role.USER
        );

        // Create another JwtUtil with a different secret
        JwtService anotherJwtService = new JwtService("different_secret_key_that_is_long_enough_12345", EXPIRATION_SECONDS);

        // Act
        boolean valid = anotherJwtService.isTokenValid(token);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    void should_throw_exception_for_malformed_token() {
        // Act & Assert
        assertThatThrownBy(() ->
            jwtService.extractUsername("invalid.token")
        )
            .isInstanceOf(Exception.class);
    }
}