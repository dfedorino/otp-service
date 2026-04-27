package com.dfedorino.otp.repository.transaction;

@FunctionalInterface
public interface TransactionCallback<T> {
    T doInTransaction();
}
