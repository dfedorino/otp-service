package com.dfedorino.otp.controller.dto;

public record ValidateOtpRequest(Long userId, String operationId, String code) {

}
