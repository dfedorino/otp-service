package com.dfedorino.otp.service.dto;

import com.dfedorino.otp.domain.enums.OtpStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.time.LocalDateTime;

public record OtpCodeDto(
    Long userId,
    String operationId,
    String code,
    OtpStatus status,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant expiresAt
) {

}
