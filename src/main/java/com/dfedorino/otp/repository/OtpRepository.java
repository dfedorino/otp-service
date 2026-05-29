package com.dfedorino.otp.repository;

import com.dfedorino.otp.domain.enums.OtpStatus;
import com.dfedorino.otp.domain.model.OtpCode;
import java.time.Instant;
import java.util.Optional;

public interface OtpRepository {

    boolean save(long userId, String operationId, String code, OtpStatus status, Instant expiresAt);

    Optional<OtpCode> findByUserIdAndOperationIdAndCode(long userId, String operationId, String code);

    long deactivateExpired(Instant expiresAt);

    int deleteByUserId(long userId);
    
    boolean updateStatusById(long id, OtpStatus status);
}
