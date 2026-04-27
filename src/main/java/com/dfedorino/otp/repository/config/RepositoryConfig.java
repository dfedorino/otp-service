package com.dfedorino.otp.repository.config;

import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.repository.datasource.DataSource;
import com.dfedorino.otp.repository.datasource.impl.PooledDataSource;
import com.dfedorino.otp.repository.impl.JdbcOtpRepository;
import com.dfedorino.otp.repository.impl.JdbcUserRepository;
import com.dfedorino.otp.repository.transaction.TransactionManager;

public class RepositoryConfig {

    public DataSource pooledDataSource() {
        return new PooledDataSource();
    }

    public TransactionManager transactionManager() {
        return new TransactionManager(pooledDataSource());
    }

    public UserRepository userRepository() {
        return new JdbcUserRepository();
    }

    public OtpRepository otpRepository() {
        return new JdbcOtpRepository(pooledDataSource());
    }

}
