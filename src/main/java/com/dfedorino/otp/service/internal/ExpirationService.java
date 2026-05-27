package com.dfedorino.otp.service.internal;

import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.transaction.Transactional;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ExpirationService {
    private final ScheduledExecutorService executor;
    private final OtpRepository otpRepository;
    private final Properties applicationProperties;

    @Transactional
    public void start() {
        log.debug("Starting OTP expiration service");
        executor.scheduleWithFixedDelay(
            this::safeExecute,
            0,
            Long.parseLong((String) applicationProperties.get("scheduler.interval")),
            TimeUnit.valueOf((String) applicationProperties.get("scheduler.timeUnit"))
        );
        log.debug("OTP expiration service started");
    }

    private void safeExecute() {
        Instant now = Instant.now();
        try {
            long deleted = otpRepository.deleteActive(now);

            if (deleted > 0) {
                log.debug("Removed {} OTP codes with expiration before {}", deleted, now);
            } else {
                log.debug("OTP codes with expiration before {} not found", now);
            }

        } catch (Exception e) {
            log.error(">> Failed to delete active at {}", now);
            log.error(">> Exception: ", e);
        }
    }
}
