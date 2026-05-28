package com.dfedorino.otp.service;

import com.dfedorino.otp.domain.exception.AdminAlreadyExists;
import com.dfedorino.otp.domain.exception.InvalidCredentialsException;
import com.dfedorino.otp.domain.exception.LoginAlreadyExists;
import com.dfedorino.otp.domain.exception.TransactionException;
import com.dfedorino.otp.domain.exception.UserNotFoundException;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.service.config.ServiceConfig;
import com.dfedorino.otp.service.impl.DefaultJwtService;
import com.dfedorino.otp.util.PasswordUtil;
import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.common.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceIT extends AbstractIntegrationTest {
    private static final ServiceConfig SERVICE_CONFIG = new ServiceConfig();

    private AuthService authService;
    private UserRepository userRepository;
    private DefaultJwtService jwtUtil;
    
    @BeforeEach
    void setUp() {
        userRepository = REPOSITORY_CONFIG.userRepository();
        jwtUtil = SERVICE_CONFIG.jwtService();
        authService = SERVICE_CONFIG.authService(tx, REPOSITORY_CONFIG.userRepository());
    }

    @Test
    void should_register_regular_user() {
        // Act
        User user = authService.register(
            "john",
            "password123",
            Role.USER
        );
        
        // Assert
        assertThat(user.id())
            .isNotNull();
            
        assertThat(user.login())
            .isEqualTo("john");
            
        assertThat(user.role())
            .isEqualTo(Role.USER);
            
        assertThat(user.hashedPassword())
            .isNotEqualTo("password123");
            
        // Verify password hashing
        assertThat(
            PasswordUtil.matches(
                "password123",
                user.hashedPassword()
            )
        ).isTrue();
        
        // Verify persistence
        Optional<User> persisted =
            tx.execute(() -> userRepository.findByLogin("john"));
            
        assertThat(persisted)
            .isPresent();
    }
    
    @Test
    void should_register_first_admin() {
        // Act
        User admin = authService.register(
            "admin",
            "password123",
            Role.ADMIN
        );
        
        // Assert
        assertThat(admin.role())
            .isEqualTo(Role.ADMIN);
            
        assertThat(tx.execute(() -> userRepository.existsAdmin()))
            .isTrue();
    }
    
    @Test
    void should_reject_second_admin_registration() {
        // Arrange - Register first admin
        authService.register(
            "admin",
            "password123",
            Role.ADMIN
        );
        
        // Act + Assert
        assertThatThrownBy(() ->
            authService.register(
                "admin2",
                "password123",
                Role.ADMIN
            )
        )
            .isInstanceOf(TransactionException.class)
            .hasCauseInstanceOf(AdminAlreadyExists.class)
            .hasRootCauseMessage("Administrator already exists");
    }
    
    @Test
    void should_reject_duplicate_login() {
        // Arrange - Register first user
        authService.register(
            "john",
            "password123",
            Role.USER
        );
        
        // Act + Assert
        assertThatThrownBy(() ->
            authService.register(
                "john",
                "another-password",
                Role.USER
            )
        )
            .isInstanceOf(TransactionException.class)
            .hasCauseInstanceOf(LoginAlreadyExists.class)
            .hasRootCauseMessage("Login already exists");
    }
    
    @Test
    void should_login_successfully() {
        // Arrange - Register user
        authService.register(
            "john",
            "password123",
            Role.USER
        );
        
        // Act
        String token = authService.login(
            "john",
            "password123"
        );
        
        // Assert
        assertThat(token)
            .isNotBlank();
            
        assertThat(jwtUtil.isTokenValid(token))
            .isTrue();
            
        assertThat(jwtUtil.extractUsername(token))
            .isEqualTo("john");
            
        assertThat(jwtUtil.extractRole(token))
            .isEqualTo(Role.USER);
    }
    
    @Test
    void should_reject_login_for_unknown_user() {
        // Act + Assert
        assertThatThrownBy(() ->
            authService.login(
                "missing",
                "password123"
            )
        )
            .isInstanceOf(TransactionException.class)
            .hasCauseInstanceOf(UserNotFoundException.class)
            .hasRootCauseMessage("User not found by login: \"missing\"");
    }
    
    @Test
    void should_reject_login_for_invalid_password() {
        // Arrange - Register user
        authService.register(
            "john",
            "correct-password",
            Role.USER
        );
        
        // Act + Assert
        assertThatThrownBy(() ->
            authService.login(
                "john",
                "wrong-password"
            )
        )
            .isInstanceOf(TransactionException.class)
            .hasCauseInstanceOf(InvalidCredentialsException.class)
            .hasRootCauseMessage("Invalid credentials");
    }
}