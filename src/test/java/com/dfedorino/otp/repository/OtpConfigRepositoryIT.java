package com.dfedorino.otp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dfedorino.otp.common.AbstractIntegrationTest;
import com.dfedorino.otp.domain.model.OtpConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class OtpConfigRepositoryIT extends AbstractIntegrationTest {

    private OtpConfigRepository otpConfigRepository;

    @BeforeEach
    void setUp() {
        otpConfigRepository = REPOSITORY_CONFIG.otpConfigRepository();
    }

    @Test
    void should_find_config() {
        tx.executeWithoutResult(() -> {
            // Call findFirst() and assert returned config matches seeded defaults
            var result = otpConfigRepository.findFirst();
            
            assertThat(result).isPresent();
            var config = result.get();
            assertThat(config.id()).isEqualTo(1L);
            assertThat(config.codeLength()).isEqualTo(6);
            assertThat(config.ttlSeconds()).isEqualTo(300);
        });
    }

    @Test
    void should_update_config() {
        tx.executeWithoutResult(() -> {
            // Find current config and update it
            var initialConfig = otpConfigRepository.findFirst().orElseThrow();
            
            // Create updated config
            var updatedConfig = new OtpConfig(
                initialConfig.id(),
                8,
                600
            );
            
            // Update it
            boolean updated = otpConfigRepository.update(updatedConfig);
            assertThat(updated).isTrue();
            
            // Verify updated values are persisted
            var retrievedConfig = otpConfigRepository.findFirst().orElseThrow();
            assertThat(retrievedConfig.codeLength()).isEqualTo(8);
            assertThat(retrievedConfig.ttlSeconds()).isEqualTo(600);
        });
    }
}