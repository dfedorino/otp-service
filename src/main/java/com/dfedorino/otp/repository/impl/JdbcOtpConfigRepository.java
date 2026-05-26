package com.dfedorino.otp.repository.impl;

import com.dfedorino.otp.domain.model.OtpConfig;
import com.dfedorino.otp.repository.OtpConfigRepository;
import com.dfedorino.otp.repository.utils.Queries;
import com.dfedorino.otp.repository.utils.ResultSetMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcOtpConfigRepository implements OtpConfigRepository {

    private static final String SELECT_FIRST_CONFIG = "SELECT id, code_length, ttl_seconds FROM otp_config LIMIT 1";
    private static final String UPDATE_CONFIG = "UPDATE otp_config SET code_length = ?, ttl_seconds = ? WHERE id = ?";

    private static final ResultSetMapper<OtpConfig> CONFIG_RESULT_SET_MAPPER = rs -> {
        try {
            return new OtpConfig(
                rs.getLong("id"),
                rs.getInt("code_length"),
                rs.getInt("ttl_seconds")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    @Override
    public Optional<OtpConfig> findFirst() {
        log.debug("Finding first OTP config");
        return Queries.query(SELECT_FIRST_CONFIG, CONFIG_RESULT_SET_MAPPER).stream().findFirst();
    }

    @Override
    public boolean update(OtpConfig config) {
        log.debug("Updating OTP config with id: {}, code_length: {}, ttl_seconds: {}", 
            config.id(), config.codeLength(), config.ttlSeconds());
        return Queries.update(UPDATE_CONFIG, config.codeLength(), config.ttlSeconds(), config.id()) > 0;
    }
}