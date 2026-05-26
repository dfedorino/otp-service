package com.dfedorino.otp.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void should_hash_password() {
        // Arrange
        String rawPassword = "password123";

        // Act
        String hash = PasswordUtil.hash(rawPassword);

        // Assert
        assertThat(hash)
            .isNotBlank()
            .isNotEqualTo(rawPassword);
    }

    @Test
    void should_verify_correct_password() {
        // Arrange
        String rawPassword = "password123";

        String hash = PasswordUtil.hash(rawPassword);

        // Act
        boolean matches = PasswordUtil.matches(
            rawPassword,
            hash
        );

        // Assert
        assertThat(matches).isTrue();
    }

    @Test
    void should_reject_incorrect_password() {
        // Arrange
        String hash = PasswordUtil.hash("password123");

        // Act
        boolean matches = PasswordUtil.matches(
            "wrong-password",
            hash
        );

        // Assert
        assertThat(matches).isFalse();
    }

    @Test
    void should_generate_different_hashes_for_same_password() {
        // Arrange
        String rawPassword = "password123";

        // Act
        String hash1 = PasswordUtil.hash(rawPassword);
        String hash2 = PasswordUtil.hash(rawPassword);

        // Assert
        assertThat(hash1)
            .isNotEqualTo(hash2);
    }

    @Test
    void should_throw_exception_for_null_password_hashing() {
        // Act + Assert
        assertThatThrownBy(() ->
            PasswordUtil.hash(null)
        )
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_throw_exception_for_null_password_matching() {
        // Act + Assert
        assertThatThrownBy(() ->
            PasswordUtil.matches(null, "hash")
        )
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_throw_exception_for_null_hash_matching() {
        // Act + Assert
        assertThatThrownBy(() ->
            PasswordUtil.matches("password", null)
        )
            .isInstanceOf(IllegalArgumentException.class);
    }
}