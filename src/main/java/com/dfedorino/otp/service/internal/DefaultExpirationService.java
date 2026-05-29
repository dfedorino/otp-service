package com.dfedorino.otp.service.internal;

import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefaultExpirationService implements ExpirationService {
    private final OtpRepository otpRepository;

    @Transactional
    @Override
    public void start() {
        Instant now = Instant.now();
        try {
            long deleted = otpRepository.deactivateExpired(now);

            if (deleted > 0) {
                log.debug("Deactivated {} OTP codes with expiration before {}", deleted, now);
            }

        } catch (Exception e) {
            log.error(">> Failed to delete active at {}", now);
            log.error(">> Exception: ", e);
        }
    }
}
