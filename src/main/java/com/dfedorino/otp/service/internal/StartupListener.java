package com.dfedorino.otp.service.internal;

import com.dfedorino.otp.util.ApplicationPropertiesUtil;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ExpirationService expirationService;
    private final Properties applicationProperties = ApplicationPropertiesUtil.loadApplicationProperties();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.debug("Starting OTP expiration service");
        executor.scheduleWithFixedDelay(
            expirationService::start,
            0,
            Long.parseLong((String) applicationProperties.get("scheduler.interval")),
            TimeUnit.valueOf((String) applicationProperties.get("scheduler.timeUnit"))
        );
        log.debug("OTP expiration service started");
        expirationService.start();
    }
}
