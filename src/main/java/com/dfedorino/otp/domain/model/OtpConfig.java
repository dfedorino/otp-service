package com.dfedorino.otp.domain.model;

public record OtpConfig(
    Long id,
    int codeLength,
    int ttlSeconds
) {

}
