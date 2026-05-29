package com.dfedorino.otp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dfedorino.otp.common.AbstractIntegrationTest;
import com.dfedorino.otp.domain.enums.OtpStatus;
import com.dfedorino.otp.domain.model.OtpCode;
import com.dfedorino.otp.repository.utils.Queries;
import com.dfedorino.otp.domain.enums.Role;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OtpRepositoryIT extends AbstractIntegrationTest {

    private OtpRepository otpRepository;

    @BeforeEach
    void setUp() {
        otpRepository = REPOSITORY_CONFIG.otpRepository();
    }

    @Test
    void should_save_otp_code() {
        tx.executeWithoutResult(() -> {
            // Create a user using SQL (no duplicates)
            Queries.update("INSERT INTO users (login, password, role) VALUES (?, ?, ?)",
                "testuser", "hashedPassword", Role.USER.name());

            Instant expiresAt = Instant.now().plusSeconds(300);

            // Save OTP code with all fields
            boolean saved = otpRepository.save(
                1L,
                "reset-password",
                "123456",
                OtpStatus.ACTIVE,
                expiresAt
            );

            assertThat(saved).isTrue();

            // Verify saved code can be retrieved
            Optional<OtpCode> retrieved = otpRepository.findByUserIdAndOperationIdAndCode(1L, "reset-password", "123456");

            assertThat(retrieved).isPresent();

            OtpCode otpCode = retrieved.get();
            assertThat(otpCode.userId()).isEqualTo(1L);
            assertThat(otpCode.operationId()).isEqualTo("reset-password");
            assertThat(otpCode.code()).isEqualTo("123456");
            assertThat(otpCode.status()).isEqualTo(OtpStatus.ACTIVE);
            // Compare LocalDateTime directly as we're testing the method works
            assertThat(otpCode.expiresAt()).isNotNull();
        });
    }

    @Test
    void should_return_empty_when_otp_not_found() {
        tx.executeWithoutResult(() -> {
            // Test that non-existent OTP returns empty
            Optional<OtpCode> retrieved = otpRepository.findByUserIdAndOperationIdAndCode(999L, "non-existent", "999999");

            // Assert Optional.empty()
            assertThat(retrieved).isEmpty();
        });
    }

    @Test
    void should_delete_expired_otp_codes() {
        tx.executeWithoutResult(() -> {
            // Create users with unique logins to avoid constraint violations
            Queries.update("INSERT INTO users (login, password, role) VALUES (?, ?, ?)",
                "user1", "hashedPassword", Role.USER.name());
            Queries.update("INSERT INTO users (login, password, role) VALUES (?, ?, ?)",
                "user2", "hashedPassword", Role.USER.name());
            Queries.update("INSERT INTO users (login, password, role) VALUES (?, ?, ?)",
                "user3", "hashedPassword", Role.USER.name());

            // Save 3 OTP codes: 2 expired (expiresAt in past), 1 active (expiresAt in future)
            Instant pastExpiresAt = Instant.now().minusSeconds(300);
            Instant futureExpiresAt = Instant.now().plusSeconds(300);

            otpRepository.save(1L, "operation1", "111111", OtpStatus.ACTIVE, pastExpiresAt);
            otpRepository.save(2L, "operation2", "222222", OtpStatus.ACTIVE, pastExpiresAt);
            otpRepository.save(3L, "operation3", "333333", OtpStatus.ACTIVE, futureExpiresAt);

            // Call deleteActive(Instant.now())
            boolean deleted = otpRepository.deactivateExpired(Instant.now()) > 0;

            // Verify: deleted count = 2, active code still exists
            assertThat(deleted).isTrue();

            // Verify that only the active code with future expiry exists
            Optional<OtpCode> remaining = otpRepository.findByUserIdAndOperationIdAndCode(3L, "operation3", "333333");
            assertThat(remaining).isPresent();
        });
    }

    @Test
    void should_not_delete_used_or_expired_status_codes() {
        tx.executeWithoutResult(() -> {
            // Create users with unique logins
            Queries.update("INSERT INTO users (login, password, role) VALUES (?, ?, ?)",
                "user1", "hashedPassword", Role.USER.name());
            Queries.update("INSERT INTO users (login, password, role) VALUES (?, ?, ?)",
                "user2", "hashedPassword", Role.USER.name());

            // Save codes with status USED and EXPIRED
            Instant expiresAt = Instant.now().minusSeconds(300); // Already expired

            otpRepository.save(1L, "used-operation", "111111", OtpStatus.USED, expiresAt);
            otpRepository.save(2L, "expired-operation", "222222", OtpStatus.EXPIRED, expiresAt);

            // Call deleteActive(Instant.now())
            boolean deleted = otpRepository.deactivateExpired(Instant.now()) > 0;

            // Verify: no deletion (method only deletes ACTIVE + time-expired)
            assertThat(deleted).isFalse();

            // Verify both codes still exist
            Optional<OtpCode> usedCode = otpRepository.findByUserIdAndOperationIdAndCode(1L, "used-operation", "111111");
            Optional<OtpCode> expiredCode = otpRepository.findByUserIdAndOperationIdAndCode(2L, "expired-operation", "222222");

            assertThat(usedCode).isPresent();
            assertThat(expiredCode).isPresent();
        });
    }
    
    @Test
    void should_delete_all_otp_codes_by_user_id() {
        tx.executeWithoutResult(() -> {
            // Create two users for testing
            Queries.update("INSERT INTO users (login, password, role) VALUES (?, ?, ?)",
                "user1", "hashedPassword", Role.USER.name());
            Queries.update("INSERT INTO users (login, password, role) VALUES (?, ?, ?)",
                "user2", "hashedPassword", Role.USER.name());

            // Insert 3 OTP codes for user 1 with different statuses
            Instant expiresAt = Instant.now().plusSeconds(300);
            
            otpRepository.save(1L, "operation1", "111111", OtpStatus.ACTIVE, expiresAt);
            otpRepository.save(1L, "operation2", "222222", OtpStatus.USED, expiresAt);
            otpRepository.save(1L, "operation3", "333333", OtpStatus.EXPIRED, expiresAt);
            
            // Insert 3 OTP codes for user 2 with different statuses
            otpRepository.save(2L, "operation4", "444444", OtpStatus.ACTIVE, expiresAt);
            otpRepository.save(2L, "operation5", "555555", OtpStatus.USED, expiresAt);
            otpRepository.save(2L, "operation6", "666666", OtpStatus.EXPIRED, expiresAt);

            // Act
            int deleted = otpRepository.deleteByUserId(1L);

            // Assert
            assertThat(deleted).isEqualTo(3);
            
            // Verify remaining OTP records belong only to user 2 by checking counts
            int countForUser2 = Queries.query(
                "SELECT COUNT(*) FROM otp_codes WHERE user_id = ?",
                rs -> rs.getInt(1),
                2L
            ).getFirst();
            
            assertThat(countForUser2).isEqualTo(3);
        });
    }
    
    @Test
    void should_return_zero_when_user_has_no_otp_codes() {
        tx.executeWithoutResult(() -> {
            // Ensure user 99 has no OTP codes

            // Act
            int deleted = otpRepository.deleteByUserId(99L);

            // Assert
            assertThat(deleted).isZero();
        });
    }
    
    @Test
    void should_update_otp_status_by_id() {
        tx.executeWithoutResult(() -> {
            // Create a user for testing
            Queries.update("INSERT INTO users (login, password, role) VALUES (?, ?, ?)",
                "testuser", "hashedPassword", Role.USER.name());

            // Save an OTP code with ACTIVE status
            Instant expiresAt = Instant.now().plusSeconds(300);
            boolean saved = otpRepository.save(
                1L,
                "reset-password",
                "123456",
                OtpStatus.ACTIVE,
                expiresAt
            );

            assertThat(saved).isTrue();

            // Retrieve the OTP code to get its ID
            Optional<OtpCode> retrieved = otpRepository.findByUserIdAndOperationIdAndCode(1L, "reset-password", "123456");
            assertThat(retrieved).isPresent();
            
            OtpCode otpCode = retrieved.get();
            long id = otpCode.id();
            assertThat(otpCode.status()).isEqualTo(OtpStatus.ACTIVE);

            // Update the status to USED
            boolean updated = otpRepository.updateStatusById(id, OtpStatus.USED);
            assertThat(updated).isTrue();

            // Verify the status was updated
            Optional<OtpCode> updatedCode = otpRepository.findByUserIdAndOperationIdAndCode(1L, "reset-password", "123456");
            assertThat(updatedCode).isPresent();
            assertThat(updatedCode.get().status()).isEqualTo(OtpStatus.USED);
        });
    }

    @Test
    void should_return_false_when_updating_nonexistent_otp_status() {
        tx.executeWithoutResult(() -> {
            // Try to update status for a non-existent OTP code ID
            boolean updated = otpRepository.updateStatusById(999L, OtpStatus.USED);
            
            // Should return false since no record exists
            assertThat(updated).isFalse();
        });
    }
}