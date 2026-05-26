package com.dfedorino.otp.service.config;

import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.repository.transaction.TransactionalProxy;
import com.dfedorino.otp.service.AuthService;
import com.dfedorino.otp.service.AuthServiceImpl;
import com.dfedorino.otp.service.JwtService;
import com.dfedorino.otp.util.ApplicationPropertiesUtil;
import java.util.Properties;

public class ServiceConfig {
    private final RepositoryConfig repositoryConfig = new RepositoryConfig();
    private final Properties props = ApplicationPropertiesUtil.loadApplicationProperties();

    public JwtService jwtService() {
        String jwtSecret = props.getProperty("jwt.secret");
        long jwtExpiration = Long.parseLong(props.getProperty("jwt.expiration.seconds"));
        return new JwtService(jwtSecret, jwtExpiration);
    }

    public AuthService authService() {
        var txManager = repositoryConfig.transactionManager();
        AuthService impl = new AuthServiceImpl(
            repositoryConfig.userRepository(),
            jwtService()
        );
        return TransactionalProxy.create(impl, txManager);
    }

}