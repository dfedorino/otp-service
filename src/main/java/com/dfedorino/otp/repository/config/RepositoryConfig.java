package com.dfedorino.otp.repository.config;

import com.dfedorino.otp.repository.OtpConfigRepository;
import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.repository.datasource.DataSource;
import com.dfedorino.otp.repository.datasource.impl.PooledDataSource;
import com.dfedorino.otp.repository.impl.JdbcOtpConfigRepository;
import com.dfedorino.otp.repository.impl.JdbcOtpRepository;
import com.dfedorino.otp.repository.impl.JdbcUserRepository;
import com.dfedorino.otp.repository.transaction.TransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public DataSource pooledDataSource() {
        return new PooledDataSource();
    }

    @Bean
    public TransactionManager transactionManager(DataSource dataSource) {
        return new TransactionManager(dataSource);
    }

    @Bean
    public UserRepository userRepository() {
        return new JdbcUserRepository();
    }

    @Bean
    public OtpRepository otpRepository() {
        return new JdbcOtpRepository();
    }

    @Bean
    public OtpConfigRepository otpConfigRepository() {
        return new JdbcOtpConfigRepository();
    }

}
