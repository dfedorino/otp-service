package com.dfedorino.otp.domain.exception;

public class LoginAlreadyExists extends RuntimeException {
    public LoginAlreadyExists(String message) {
        super(message);
    }
}
