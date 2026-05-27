package com.dfedorino.otp.service;

import com.dfedorino.otp.service.dto.OtpCodeDto;

public interface UserService {

    OtpCodeDto generateOtp(
        long userId,
        String operationId
    );

    boolean validateOtp(
        long userId,
        String operationId,
        String code
    );
}