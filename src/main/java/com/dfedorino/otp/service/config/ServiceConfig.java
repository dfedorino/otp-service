package com.dfedorino.otp.service.config;

import com.dfedorino.otp.delivery.DeliveryChannel;
import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.repository.transaction.TransactionalProxy;
import com.dfedorino.otp.service.AdminService;
import com.dfedorino.otp.service.AuthService;
import com.dfedorino.otp.service.UserService;
import com.dfedorino.otp.service.impl.AdminServiceImpl;
import com.dfedorino.otp.service.impl.DefaultAuthService;
import com.dfedorino.otp.service.impl.DefaultJwtService;
import com.dfedorino.otp.service.impl.DefaultUserService;
import com.dfedorino.otp.util.ApplicationPropertiesUtil;
import java.util.List;
import java.util.Properties;

public class ServiceConfig {
    private final RepositoryConfig repositoryConfig = new RepositoryConfig();
    private final Properties props = ApplicationPropertiesUtil.loadApplicationProperties();

    public DefaultJwtService jwtService() {
        String jwtSecret = props.getProperty("jwt.secret");
        long jwtExpiration = Long.parseLong(props.getProperty("jwt.expiration.seconds"));
        return new DefaultJwtService(jwtSecret, jwtExpiration);
    }

    public AuthService authService() {
        var txManager = repositoryConfig.transactionManager();
        AuthService impl = new DefaultAuthService(
            repositoryConfig.userRepository(),
            jwtService()
        );
        return TransactionalProxy.create(impl, txManager);
    }

    public UserService userService(List<DeliveryChannel> deliveryChannels) {
        var txManager = repositoryConfig.transactionManager();
        UserService impl = new DefaultUserService(
            repositoryConfig.userRepository(),
            repositoryConfig.otpRepository(),
            repositoryConfig.otpConfigRepository(),
            deliveryChannels
        );
        return TransactionalProxy.create(impl, txManager);
    }

    public AdminService adminService() {
        var txManager = repositoryConfig.transactionManager();
        AdminService impl = new AdminServiceImpl(
            repositoryConfig.userRepository(),
            repositoryConfig.otpRepository(),
            repositoryConfig.otpConfigRepository()
        );
        return TransactionalProxy.create(impl, txManager);
    }
}