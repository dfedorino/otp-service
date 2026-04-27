package com.dfedorino.otp.domain.exception;

public class TransactionException extends RuntimeException {

    public TransactionException(Exception e) {
        super(e);
    }

}
