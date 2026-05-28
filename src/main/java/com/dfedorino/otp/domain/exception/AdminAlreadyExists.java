package com.dfedorino.otp.domain.exception;

public class AdminAlreadyExists extends RuntimeException {
    public AdminAlreadyExists(String message) {
        super(message);
    }
}
