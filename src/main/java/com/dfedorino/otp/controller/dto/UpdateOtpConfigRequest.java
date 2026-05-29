package com.dfedorino.otp.controller.dto;

public record UpdateOtpConfigRequest(
    int codeLength,
    int ttlSeconds
) {

}
