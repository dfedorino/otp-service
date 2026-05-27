package com.dfedorino.otp.service.dto;

import com.dfedorino.otp.domain.enums.OtpStatus;
import java.time.Instant;

public record OtpCodeDto(
    Long userId,
    String operationId,
    String code,
    OtpStatus status,
    Instant expiresAt
) {

}
