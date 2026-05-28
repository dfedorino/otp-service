package com.dfedorino.otp.service.config;

import com.dfedorino.otp.delivery.DeliveryChannel;
import com.dfedorino.otp.repository.OtpConfigRepository;
import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.repository.transaction.TransactionManager;
import com.dfedorino.otp.repository.transaction.TransactionalProxy;
import com.dfedorino.otp.service.AdminService;
import com.dfedorino.otp.service.AuthService;
import com.dfedorino.otp.service.UserService;
import com.dfedorino.otp.service.impl.AdminServiceImpl;
import com.dfedorino.otp.service.impl.DefaultAuthService;
import com.dfedorino.otp.service.impl.DefaultJwtService;
import com.dfedorino.otp.service.impl.DefaultUserService;
import com.dfedorino.otp.service.internal.DefaultExpirationService;
import com.dfedorino.otp.service.internal.ExpirationService;
import com.dfedorino.otp.util.ApplicationPropertiesUtil;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ServiceConfig {
    private final Properties props = ApplicationPropertiesUtil.loadApplicationProperties();

    @Bean
    public DefaultJwtService jwtService() {
        String jwtSecret = props.getProperty("jwt.secret");
        long jwtExpiration = Long.parseLong(props.getProperty("jwt.expiration.seconds"));
        return new DefaultJwtService(jwtSecret, jwtExpiration);
    }

    @Bean
    public AuthService authService(TransactionManager txManager, UserRepository userRepository) {
        log.debug(">> Creating auth service");
        AuthService impl = new DefaultAuthService(
            userRepository,
            jwtService()
        );
        return TransactionalProxy.create(impl, txManager);
    }

    @Bean
    public UserService userService(
        List<DeliveryChannel> deliveryChannels,
        TransactionManager txManager,
        UserRepository userRepository,
        OtpRepository otpRepository,
        OtpConfigRepository otpConfigRepository
    ) {
        UserService impl = new DefaultUserService(
            userRepository,
            otpRepository,
            otpConfigRepository,
            deliveryChannels
        );
        return TransactionalProxy.create(impl, txManager);
    }

    @Bean
    public AdminService adminService(
        TransactionManager txManager,
        UserRepository userRepository,
        OtpRepository otpRepository,
        OtpConfigRepository otpConfigRepository
    ) {
        AdminService impl = new AdminServiceImpl(
            userRepository,
            otpRepository,
            otpConfigRepository
        );
        return TransactionalProxy.create(impl, txManager);
    }

    @Bean
    public ExpirationService expirationService(
        TransactionManager txManager,
        OtpRepository otpRepository
    ) {
        ExpirationService impl = new DefaultExpirationService(
            Executors.newSingleThreadScheduledExecutor(),
            otpRepository,
            props
        );
        return TransactionalProxy.create(impl, txManager);
    }
}