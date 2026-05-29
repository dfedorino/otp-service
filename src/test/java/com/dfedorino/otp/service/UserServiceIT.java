package com.dfedorino.otp.service;

import com.dfedorino.otp.delivery.DeliveryChannel;
import com.dfedorino.otp.domain.enums.OtpStatus;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.model.OtpCode;
import com.dfedorino.otp.domain.model.OtpConfig;
import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.common.AbstractIntegrationTest;
import com.dfedorino.otp.repository.OtpConfigRepository;
import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.service.config.ServiceConfig;
import com.dfedorino.otp.service.dto.OtpCodeDto;
import com.dfedorino.otp.service.dto.UserDto;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceIT extends AbstractIntegrationTest {
    
    private static final ServiceConfig SERVICE_CONFIG = new ServiceConfig();

    private UserService userService;
    private UserRepository userRepository;
    private OtpRepository otpRepository;
    private OtpConfigRepository otpConfigRepository;
    @Mock
    private DeliveryChannel deliveryChannel;
    
    @BeforeEach
    void setUp() {
        userRepository = REPOSITORY_CONFIG.userRepository();
        otpRepository = REPOSITORY_CONFIG.otpRepository();
        otpConfigRepository = REPOSITORY_CONFIG.otpConfigRepository();

        userService = SERVICE_CONFIG.userService(List.of(deliveryChannel), tx, userRepository, otpRepository, otpConfigRepository);
    }
    
    @Test
    void should_generate_otp_with_default_otp_config() {
        // Arrange - Insert one USER user
        long userId = tx.execute(() -> {
            userRepository.save("test@example.com", "", "hashedPassword", Role.USER);
            Optional<User> found = userRepository.findByLogin("test@example.com");
            assertThat(found).isPresent();
            return found.get().id();
        });
        
        // Act
        OtpCodeDto otp = userService.generateOtp(userId, "operationId");
        
        // Assert
        assertThat(otp.userId()).isEqualTo(userId);
        assertThat(otp.operationId()).isEqualTo("operationId");
        assertThat(otp.code()).hasSize(6); // Default code length
        assertThat(otp.status()).isEqualTo(OtpStatus.ACTIVE);
        
        // Verify that expiration is within the default TTL (we expect around 300 seconds)
        Instant now = Instant.now();
        assertThat(otp.expiresAt()).isAfter(now);
        assertThat(otp.expiresAt()).isBefore(now.plusSeconds(310)); // Allow some tolerance

        verify(deliveryChannel).deliver(any(UserDto.class), any(OtpCodeDto.class));

    }
    
    @Test
    void should_generate_unique_otp_codes() {
        // Arrange - Insert one USER user
        
        long userId = tx.execute(() -> {
            userRepository.save("test@example.com", "", "hashedPassword", Role.USER);
            Optional<User> found = userRepository.findByLogin("test@example.com");
            assertThat(found).isPresent();
            return found.get().id();
        });
        
        // Act - Generate multiple OTPs
        OtpCodeDto otp1 = userService.generateOtp(userId, "operationId1");
        OtpCodeDto otp2 = userService.generateOtp(userId, "operationId2");
        OtpCodeDto otp3 = userService.generateOtp(userId, "operationId3");
        
        // Assert
        assertThat(otp1.userId()).isEqualTo(userId);
        assertThat(otp2.userId()).isEqualTo(userId);
        assertThat(otp3.userId()).isEqualTo(userId);
        
        // Codes should be different
        assertThat(otp1.code()).isNotEqualTo(otp2.code());
        assertThat(otp1.code()).isNotEqualTo(otp3.code());
        assertThat(otp2.code()).isNotEqualTo(otp3.code());

        verify(deliveryChannel, times(3)).deliver(any(UserDto.class), any(OtpCodeDto.class));
    }
    
    @Test
    void should_validate_active_otp() {
        // Arrange - Insert one USER user
        long userId = tx.execute(() -> {
            userRepository.save("test@example.com", "", "hashedPassword", Role.USER);
            Optional<User> found = userRepository.findByLogin("test@example.com");
            assertThat(found).isPresent();
            return found.get().id();
        });
        
        // Generate OTP
        OtpCodeDto otp = userService.generateOtp(userId, "operationId");
        
        // Act
        boolean result = userService.validateOtp(userId, "operationId", otp.code());
        
        // Assert
        assertThat(result).isTrue();
        
        // Verify status update
        OtpCode persisted = tx.execute(() -> otpRepository.findByUserIdAndOperationIdAndCode(userId, "operationId", otp.code()).orElseThrow());
        assertThat(persisted.status()).isEqualTo(OtpStatus.USED);
    }
    
    @Test
    void should_reject_invalid_otp() {
        // Arrange - Insert one USER user
        long userId = tx.execute(() -> {
            userRepository.save("test@example.com", "", "hashedPassword", Role.USER);
            Optional<User> found = userRepository.findByLogin("test@example.com");
            assertThat(found).isPresent();
            return found.get().id();
        });
        
        // Act
        boolean result = userService.validateOtp(userId, "operationId", "wrong-code");
        
        // Assert
        assertThat(result).isFalse();
    }
    
    @Test
    void should_reject_expired_otp() {
        // Arrange - Insert one USER user
        long userId = tx.execute(() -> {
            userRepository.save("test@example.com", "", "hashedPassword", Role.USER);
            Optional<User> found = userRepository.findByLogin("test@example.com");
            assertThat(found).isPresent();
            return found.get().id();
        });
        
        // Get current OTP config and temporarily shorten TTL for testing 
        OtpConfig originalConfig = tx.execute(() -> otpConfigRepository.findFirst().orElseThrow());
        
        // Temporarily update OTP config with a short TTL
        OtpConfig shortConfig = new OtpConfig(originalConfig.id(), originalConfig.codeLength(), 1);
        tx.executeWithoutResult(() -> otpConfigRepository.update(shortConfig));
        
        // Generate OTP - should expire quickly
        OtpCodeDto otp = userService.generateOtp(userId, "operationId");
        
        // Wait a bit for it to expire
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Reset back to original config
        tx.executeWithoutResult(() -> otpConfigRepository.update(originalConfig));
        
        // Act
        boolean result = userService.validateOtp(userId, "operationId", otp.code());
        
        // Assert
        assertThat(result).isFalse();
        
        // Verify status
        OtpCode expired = tx.execute(() -> otpRepository.findByUserIdAndOperationIdAndCode(userId, "operationId", otp.code()).orElseThrow());
        assertThat(expired.status()).isEqualTo(OtpStatus.EXPIRED);
    }
    
    @Test
    void should_reject_reused_otp() {
        // Arrange - Insert one USER user
        long userId = tx.execute(() -> {
            userRepository.save("test@example.com", "", "hashedPassword", Role.USER);
            Optional<User> found = userRepository.findByLogin("test@example.com");
            assertThat(found).isPresent();
            return found.get().id();
        });
        
        // Generate OTP
        OtpCodeDto otp = userService.generateOtp(userId, "operationId");
        
        // Validate once successfully
        boolean firstValidation = userService.validateOtp(userId, "operationId", otp.code());
        
        // Act - Try second validation
        boolean secondValidation = userService.validateOtp(userId, "operationId", otp.code());
        
        // Assert
        assertThat(firstValidation).isTrue();
        assertThat(secondValidation).isFalse();
    }
}