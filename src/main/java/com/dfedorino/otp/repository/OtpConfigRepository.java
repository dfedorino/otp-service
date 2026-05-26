package com.dfedorino.otp.repository;

import com.dfedorino.otp.domain.model.OtpConfig;
import java.util.Optional;

public interface OtpConfigRepository {
    
    Optional<OtpConfig> findFirst();
    
    boolean update(OtpConfig config);
}