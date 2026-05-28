package com.dfedorino.otp.domain.exception;

public class OtpConfigNotFoundException extends RuntimeException {
    public OtpConfigNotFoundException(String message) {
        super(message);
    }

}
