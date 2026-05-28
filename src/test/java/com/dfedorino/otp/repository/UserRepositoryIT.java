package com.dfedorino.otp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dfedorino.otp.common.AbstractIntegrationTest;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserRepositoryIT extends AbstractIntegrationTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = REPOSITORY_CONFIG.userRepository();
    }

    @Test
    void should_create_user() {
        tx.executeWithoutResult(() -> {
            assertThat(userRepository.save(
                "login",
                "hashedPassword",
                Role.USER
            )).isTrue();

            var actual = userRepository.findByLogin("login");
            var expected = new User(1L, "login", "hashedPassword", Role.USER);
            assertThat(actual).contains(expected);
            assertThat(userRepository.findById(1L)).contains(expected);
        });
    }

    @Test
    void should_delete_user() {
        tx.executeWithoutResult(() -> {
            assertThat(userRepository.save(
                "login",
                "hashedPassword",
                Role.USER
            )).isTrue();

            assertThat(userRepository.deleteById(1L)).isTrue();

            assertThat(userRepository.findByLogin("login")).isEmpty();
            assertThat(userRepository.findById(1L)).isEmpty();
        });
    }
    
    @Test
    void should_return_true_when_admin_exists() {
        tx.executeWithoutResult(() -> {
            // Arrange
            assertThat(userRepository.save(
                "admin",
                "hashedPassword",
                Role.ADMIN
            )).isTrue();
            
            // Act
            boolean exists = userRepository.existsAdmin();
            
            // Assert
            assertThat(exists).isTrue();
        });
    }
    
    @Test
    void should_return_false_when_admin_does_not_exist() {
        tx.executeWithoutResult(() -> {
            // Arrange
            assertThat(userRepository.save(
                "user1",
                "hashedPassword",
                Role.USER
            )).isTrue();
            
            assertThat(userRepository.save(
                "user2",
                "hashedPassword",
                Role.USER
            )).isTrue();
            
            // Act
            boolean exists = userRepository.existsAdmin();
            
            // Assert
            assertThat(exists).isFalse();
        });
    }
    
    @Test
    void should_ignore_regular_users_when_checking_admin_existence() {
        tx.executeWithoutResult(() -> {
            // Arrange
            assertThat(userRepository.save(
                "user1",
                "hashedPassword",
                Role.USER
            )).isTrue();
            
            assertThat(userRepository.save(
                "user2",
                "hashedPassword",
                Role.USER
            )).isTrue();
            
            assertThat(userRepository.save(
                "user3",
                "hashedPassword",
                Role.USER
            )).isTrue();
            
            // Act
            boolean exists = userRepository.existsAdmin();
            
            // Assert
            assertThat(exists).isFalse();
        });
    }
}
