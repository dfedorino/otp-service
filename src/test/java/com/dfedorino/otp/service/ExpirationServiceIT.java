package com.dfedorino.otp.service;

import com.dfedorino.otp.domain.enums.OtpStatus;
import com.dfedorino.otp.domain.model.OtpCode;
import com.dfedorino.otp.domain.model.OtpConfig;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpirationServiceIT extends AbstractIntegrationTest {

    private static final ServiceConfig SERVICE_CONFIG = new ServiceConfig();

    private DefaultExpirationService expirationService;
    private AdminService adminService;
    private UserService userService;
    private UserRepository userRepository;
    private OtpRepository otpRepository;
    
    @Mock
    private ScheduledExecutorService mockScheduledExecutor;

    @BeforeEach
    void setUp() {
        userRepository = REPOSITORY_CONFIG.userRepository();
        otpRepository = REPOSITORY_CONFIG.otpRepository();

        // Set up properties for scheduler
        Properties properties = new Properties();
        properties.setProperty("scheduler.interval", "1");
        properties.setProperty("scheduler.timeUnit", "SECONDS");
        
        expirationService = new DefaultExpirationService(mockScheduledExecutor, otpRepository, properties);
        adminService = SERVICE_CONFIG.adminService(tx, userRepository, otpRepository, REPOSITORY_CONFIG.otpConfigRepository());
        userService = SERVICE_CONFIG.userService(List.of(), tx, userRepository, otpRepository, REPOSITORY_CONFIG.otpConfigRepository());

        given(mockScheduledExecutor.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
            .will(i -> {
                i.getArgument(0, Runnable.class).run();
                return null;
            });
    }

    @Test
    void should_delete_expired_otp_codes() throws InterruptedException {
        // Arrange - Insert one USER user
        long userId = tx.execute(() -> {
            userRepository.save("test@example.com", "hashedPassword", com.dfedorino.otp.domain.enums.Role.USER);
            Optional<User> found = userRepository.findByLogin("test@example.com");
            assertThat(found).isPresent();
            return found.get().id();
        });

        adminService.updateOtpConfig(new OtpConfig(1L, 6, 1));
        String operationId = "operationId";

        OtpCodeDto otp = userService.generateOtp(userId, operationId);

        // Verify that OTP code exists
        Optional<OtpCode> otpBefore = tx.execute(() -> otpRepository.findByUserIdAndOperationIdAndCode(userId, operationId, otp.code()));
        assertThat(otpBefore).isPresent();
        assertThat(otpBefore.get().status()).isEqualTo(OtpStatus.ACTIVE);

        // Act - Call the method directly to test deletion
        TimeUnit.SECONDS.sleep(1L);
        tx.executeWithoutResult(() -> expirationService.start());

        // Verify that OTP code no longer exists
        Optional<OtpCode> otpAfter = tx.execute(() -> otpRepository.findByUserIdAndOperationIdAndCode(userId, operationId, otp.code()));
        assertThat(otpAfter).isEmpty();
    }

    @Test
    void should_not_delete_active_otp_codes() {
        // Arrange - Insert one USER user
        long userId = tx.execute(() -> {
            userRepository.save("test@example.com", "hashedPassword", com.dfedorino.otp.domain.enums.Role.USER);
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