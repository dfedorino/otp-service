package com.dfedorino.otp.service;

import com.dfedorino.otp.controller.dto.UpdateOtpConfigRequest;
import com.dfedorino.otp.domain.enums.OtpStatus;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.model.OtpCode;
import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.common.AbstractIntegrationTest;
import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.service.config.ServiceConfig;
import com.dfedorino.otp.service.dto.OtpCodeDto;
import com.dfedorino.otp.service.internal.DefaultExpirationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ExpirationServiceIT extends AbstractIntegrationTest {

    private static final ServiceConfig SERVICE_CONFIG = new ServiceConfig();

    private DefaultExpirationService expirationService;
    private AdminService adminService;
    private UserService userService;
    private UserRepository userRepository;
    private OtpRepository otpRepository;

    @BeforeEach
    void setUp() {
        userRepository = REPOSITORY_CONFIG.userRepository();
        otpRepository = REPOSITORY_CONFIG.otpRepository();
        
        expirationService = new DefaultExpirationService(otpRepository);
        adminService = SERVICE_CONFIG.adminService(tx, userRepository, otpRepository, REPOSITORY_CONFIG.otpConfigRepository());
        userService = SERVICE_CONFIG.userService(List.of(), tx, userRepository, otpRepository, REPOSITORY_CONFIG.otpConfigRepository());
    }

    @Test
    void should_delete_expired_otp_codes() throws InterruptedException {
        // Arrange - Insert one USER user
        long userId = tx.execute(() -> {
            userRepository.save("test@example.com", "", "hashedPassword", Role.USER);
            Optional<User> found = userRepository.findByLogin("test@example.com");
            assertThat(found).isPresent();
            return found.get().id();
        });

        adminService.updateOtpConfig(new UpdateOtpConfigRequest(6, 1));
        String operationId = "operationId";

        OtpCodeDto otp = userService.generateOtp(userId, operationId);

        // Verify that OTP code exists
        Optional<OtpCode> otpBefore = tx.execute(() -> otpRepository.findByUserIdAndOperationIdAndCode(userId, operationId, otp.code()));
        assertThat(otpBefore).isPresent();
        assertThat(otpBefore.get().status()).isEqualTo(OtpStatus.ACTIVE);

        // Act - Call the method directly to test deletion
        TimeUnit.SECONDS.sleep(1L);
        tx.executeWithoutResult(() -> expirationService.start());

        // Verify that OTP code marked as expired
        Optional<OtpCode> otpAfter = tx.execute(() -> otpRepository.findByUserIdAndOperationIdAndCode(userId, operationId, otp.code()));
        assertThat(otpAfter).isNotEmpty().get().satisfies(code -> assertThat(code.status()).isEqualTo(OtpStatus.EXPIRED));
    }

    @Test
    void should_not_delete_active_otp_codes() {
        // Arrange - Insert one USER user
        long userId = tx.execute(() -> {
            userRepository.save("test@example.com", "", "hashedPassword", Role.USER);
            Optional<User> found = userRepository.findByLogin("test@example.com");
            assertThat(found).isPresent();
            return found.get().id();
        });

        // Generate an OTP with a future expiration time
        String operationId = "operationId";
        OtpCodeDto otp = userService.generateOtp(userId, operationId);

        // Verify that OTP code exists and is active
        Optional<OtpCode> otpBefore = tx.execute(() -> otpRepository.findByUserIdAndOperationIdAndCode(userId, operationId, otp.code()));
        assertThat(otpBefore).isPresent();
        assertThat(otpBefore.get().status()).isEqualTo(OtpStatus.ACTIVE);

        // Act - Call the method directly to test deletion
        tx.executeWithoutResult(() -> expirationService.start());

        // Verify that OTP code still exists
        Optional<OtpCode> otpAfter = tx.execute(() -> otpRepository.findByUserIdAndOperationIdAndCode(userId, operationId, otp.code()));
        assertThat(otpAfter).isPresent();
        assertThat(otpAfter.get().status()).isEqualTo(OtpStatus.ACTIVE);
    }
}