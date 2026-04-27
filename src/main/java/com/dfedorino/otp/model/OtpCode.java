package com.dfedorino.otp.model;

import com.dfedorino.otp.model.enums.OtpStatus;
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
