package com.dfedorino.otp.service.internal;

import com.dfedorino.otp.repository.transaction.Transactional;

public interface ExpirationService {

    @Transactional
    void start();
}
