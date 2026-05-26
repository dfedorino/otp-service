package com.dfedorino.otp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dfedorino.otp.domain.model.OtpConfig;
import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.repository.datasource.DataSource;
import com.dfedorino.otp.repository.transaction.TransactionManager;
import com.dfedorino.otp.repository.utils.Queries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OtpConfigRepositoryIT extends AbstractIntegrationTest {

    private final RepositoryConfig repositoryConfig = new RepositoryConfig();
    private DataSource dataSource;
    private TransactionManager tx;
    private OtpConfigRepository otpConfigRepository;

    @BeforeEach
    public void setUp() {
        dataSource = repositoryConfig.pooledDataSource();
        otpConfigRepository = repositoryConfig.otpConfigRepository();
        tx = new TransactionManager(dataSource);
    }

    @AfterEach
    void tearDown() {
        tx.executeWithoutResult(() ->
            Queries.update("UPDATE otp_config SET code_length = 6, ttl_seconds = 300"));
        dataSource.close();
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