package com.dfedorino.otp.domain.model;

import com.dfedorino.otp.domain.enums.OtpStatus;
import java.time.LocalDateTime;

public record OtpCode(
    Long id,
    Long userId,
    String operationId,
    String code,
    OtpStatus status,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {

}
