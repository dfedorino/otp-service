package com.dfedorino.otp.model;

public record OtpConfig(
    Long id,
    int codeLength,
    int ttlSeconds
) {

}
