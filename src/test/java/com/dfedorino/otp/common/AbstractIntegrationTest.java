package com.dfedorino.otp.common;

import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.repository.datasource.DataSource;
import com.dfedorino.otp.repository.transaction.TransactionManager;
import com.dfedorino.otp.repository.utils.Queries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {
    protected static final RepositoryConfig REPOSITORY_CONFIG = new RepositoryConfig();

    protected DataSource dataSource;
    protected TransactionManager tx;

    @BeforeEach
    void setUp() {
        dataSource = REPOSITORY_CONFIG.pooledDataSource();
        tx = REPOSITORY_CONFIG.transactionManager(dataSource);
    }

    @AfterEach
    void tearDown() {
        tx.executeWithoutResult(
            () -> {
                Queries.update("TRUNCATE TABLE otp_codes RESTART IDENTITY CASCADE");
                Queries.update("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
                Queries.update("""
                INSERT INTO users (login, password, "role")
                VALUES ('admin', '$2a$04$i0o7w7IQsZQuuGL..Z6G9uGbG.PzrwKUAMkfA8pxs355ZgwQpfNIi', 'ADMIN');
                """);
                Queries.update("UPDATE otp_config SET code_length = 6, ttl_seconds = 300");
            });
        dataSource.close();
    }

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("otp_db")
            .withUsername("admin")
            .withPassword("admin")
            .withInitScript("schema.sql");

    @BeforeAll
    static void init() {
        System.setProperty("POSTGRES_URL", POSTGRES.getJdbcUrl());
        System.setProperty("POSTGRES_USER", POSTGRES.getUsername());
        System.setProperty("POSTGRES_PASSWORD", POSTGRES.getPassword());
    }
}
