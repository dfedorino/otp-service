package com.dfedorino.otp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dfedorino.otp.domain.enums.OtpStatus;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.exception.TransactionException;
import com.dfedorino.otp.domain.model.OtpConfig;
import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.repository.AbstractIntegrationTest;
import com.dfedorino.otp.repository.OtpConfigRepository;
import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.repository.transaction.TransactionManager;
import com.dfedorino.otp.repository.utils.Queries;
import com.dfedorino.otp.service.config.ServiceConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AdminServiceIT extends AbstractIntegrationTest {

    private TransactionManager tx;
    private AdminService adminService;
    private UserRepository userRepository;
    private OtpRepository otpRepository;
    private OtpConfigRepository otpConfigRepository;

    @BeforeEach
    public void setUp() {
        RepositoryConfig repositoryConfig = new RepositoryConfig();
        ServiceConfig serviceConfig = new ServiceConfig();
        tx = repositoryConfig.transactionManager();
        userRepository = repositoryConfig.userRepository();
        otpRepository = repositoryConfig.otpRepository();
        otpConfigRepository = repositoryConfig.otpConfigRepository();
        adminService = serviceConfig.adminService();
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
    public void should_return_only_non_admin_users() {
        // Arrange
        tx.executeWithoutResult(() -> {
            userRepository.save("admin1", "hashed", Role.ADMIN);
            userRepository.save("user1", "hashed", Role.USER);
            userRepository.save("user2", "hashed", Role.USER);
        });

        // Act
        List<User> users = adminService.getUsers();

        // Assert
        assertThat(users)
            .hasSize(2)
            .allMatch(user -> user.role() == Role.USER);
    }

    @Test
    public void should_delete_user_and_otp_codes() {
        // Arrange
        long userId = tx.execute(() -> {
            userRepository.save("testuser", "hashed", Role.USER);
            return userRepository.findByLogin("testuser")
                .orElseThrow()
                .id();
        });

        // Insert some OTP codes for the user
        tx.executeWithoutResult(() -> {
            // Need to use proper enum since we're passing null causing errors
            otpRepository.save(userId, "operation1", "123456", OtpStatus.ACTIVE,
                Instant.now().plusSeconds(300));
            otpRepository.save(userId, "operation2", "654321", OtpStatus.ACTIVE,
                Instant.now().plusSeconds(300));
        });

        // Verify existence before deletion
        assertThat(tx.execute(
            () -> otpRepository.findByUserIdAndOperationIdAndCode(userId, "operation1", "123456")))
            .isPresent();
        assertThat(tx.execute(
            () -> otpRepository.findByUserIdAndOperationIdAndCode(userId, "operation2", "654321")))
            .isPresent();

        // Act
        adminService.deleteUser(userId);

        // Assert
        assertThat(tx.execute(() -> userRepository.findById(userId)))
            .isEmpty();
        assertThat(tx.execute(
            () -> otpRepository.findByUserIdAndOperationIdAndCode(userId, "operation1", "123456")))
            .isEmpty();
        assertThat(tx.execute(
            () -> otpRepository.findByUserIdAndOperationIdAndCode(userId, "operation2", "654321")))
            .isEmpty();
    }

    @Test
    public void should_reject_admin_deletion() {
        // Arrange
        long adminId = tx.execute(() -> {
            userRepository.save("admin", "hashed", Role.ADMIN);
            return userRepository.findByLogin("admin")
                .orElseThrow()
                .id();
        });

        // Act + Assert - Unwrap the TransactionException to check actual cause
        assertThatThrownBy(() ->
            adminService.deleteUser(adminId)
        )
            .isInstanceOf(TransactionException.class)
            .hasCauseInstanceOf(IllegalStateException.class)
            .hasRootCauseMessage("Cannot delete admin user");
    }

    @Test
    public void should_reject_missing_user_deletion() {
        // Act + Assert - Unwrap the TransactionException to check actual cause
        assertThatThrownBy(() ->
            adminService.deleteUser(999L)
        )
            .isInstanceOf(TransactionException.class)
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasRootCauseMessage("User not found");
    }

    @Test
    public void should_update_otp_config() {
        // Arrange
        OtpConfig config = tx.execute(() -> {
            otpConfigRepository.update(new OtpConfig(null, 6, 300));
            return otpConfigRepository.findFirst()
                .orElseThrow();
        });

        // Modify values
        OtpConfig modifiedConfig = new OtpConfig(config.id(), 8, 600);

        // Act
        OtpConfig updated = adminService.updateOtpConfig(modifiedConfig);

        // Assert
        assertThat(updated.ttlSeconds())
            .isEqualTo(modifiedConfig.ttlSeconds());

        assertThat(updated.codeLength())
            .isEqualTo(modifiedConfig.codeLength());

        // Verify persistence
        OtpConfig persisted =
            tx.execute(() -> otpConfigRepository.findFirst()).orElseThrow();

        assertThat(persisted.ttlSeconds())
            .isEqualTo(modifiedConfig.ttlSeconds());
    }
}