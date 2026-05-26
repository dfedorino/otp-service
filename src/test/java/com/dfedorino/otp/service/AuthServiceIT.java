package com.dfedorino.otp.service;

import com.dfedorino.otp.domain.exception.TransactionException;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.repository.transaction.TransactionManager;
import com.dfedorino.otp.repository.utils.Queries;
import com.dfedorino.otp.service.config.ServiceConfig;
import com.dfedorino.otp.util.PasswordUtil;
import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.repository.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AuthServiceIT extends AbstractIntegrationTest {

    private TransactionManager tx;
    private AuthService authService;
    private UserRepository userRepository;
    private JwtService jwtUtil;
    
    @BeforeEach
    public void setUp() {
        RepositoryConfig repositoryConfig = new RepositoryConfig();
        ServiceConfig serviceConfig = new ServiceConfig();
        tx = repositoryConfig.transactionManager();
        userRepository = repositoryConfig.userRepository();
        jwtUtil = serviceConfig.jwtService();
        authService = serviceConfig.authService();
    }

    @AfterEach
    public void tearDown() {
        tx.executeWithoutResult(
            () -> {
                Queries.update("TRUNCATE TABLE otp_codes RESTART IDENTITY CASCADE");
                Queries.update("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
            });
    }

    @Test
    public void should_register_regular_user() {
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
    public void should_register_first_admin() {
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
    public void should_reject_second_admin_registration() {
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
            .hasCauseInstanceOf(IllegalStateException.class)
            .hasRootCauseMessage("Administrator already exists");
    }
    
    @Test
    public void should_reject_duplicate_login() {
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
            .hasCauseInstanceOf(IllegalStateException.class)
            .hasRootCauseMessage("Login already exists");
    }
    
    @Test
    public void should_login_successfully() {
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
    public void should_reject_login_for_unknown_user() {
        // Act + Assert
        assertThatThrownBy(() ->
            authService.login(
                "missing",
                "password123"
            )
        )
            .isInstanceOf(TransactionException.class)
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasRootCauseMessage("Invalid credentials");
    }
    
    @Test
    public void should_reject_login_for_invalid_password() {
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
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasRootCauseMessage("Invalid credentials");
    }
}