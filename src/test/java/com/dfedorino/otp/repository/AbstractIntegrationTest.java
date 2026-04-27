package com.dfedorino.otp.repository;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("otp_db")
            .withUsername("admin")
            .withPassword("admin")
            .withInitScript("schema.sql");

    @BeforeAll
    static void init() {
        System.setProperty("DB_URL", POSTGRES.getJdbcUrl());
        System.setProperty("DB_USER", POSTGRES.getUsername());
        System.setProperty("DB_PASSWORD", POSTGRES.getPassword());
    }
}
