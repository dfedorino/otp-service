package com.dfedorino.otp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.repository.datasource.DataSource;
import com.dfedorino.otp.repository.transaction.TransactionManager;
import com.dfedorino.otp.repository.utils.Queries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserRepositoryIT extends AbstractIntegrationTest {

    private final RepositoryConfig repositoryConfig = new RepositoryConfig();
    private DataSource dataSource;
    private TransactionManager tx;
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        dataSource = repositoryConfig.pooledDataSource();
        userRepository = repositoryConfig.userRepository();
        tx = new TransactionManager(dataSource);
    }

    @AfterEach
    public void tearDown() {
        tx.executeWithoutResult(
            () -> Queries.update("TRUNCATE TABLE users RESTART IDENTITY CASCADE"));
        dataSource.close();
    }

    @Test
    void should_create_user() {
        tx.executeWithoutResult(() -> {
            assertThat(userRepository.save(
                "login",
                "hashedPassword",
                Role.USER
            )).isTrue();

            var actual = userRepository.findByLogin("login");
            var expected = new User(1L, "login", "hashedPassword", Role.USER);
            assertThat(actual).contains(expected);
            assertThat(userRepository.findById(1L)).contains(expected);
        });
    }

    @Test
    void should_delete_user() {
        tx.executeWithoutResult(() -> {
            assertThat(userRepository.save(
                "login",
                "hashedPassword",
                Role.USER
            )).isTrue();

            assertThat(userRepository.deleteById(1L)).isTrue();

            assertThat(userRepository.findByLogin("login")).isEmpty();
            assertThat(userRepository.findById(1L)).isEmpty();
        });
    }
}
