package com.dfedorino.otp.repository.impl;

import com.dfedorino.otp.domain.enums.OtpStatus;
import com.dfedorino.otp.domain.model.OtpCode;
import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.utils.Queries;
import com.dfedorino.otp.repository.utils.ResultSetMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcOtpRepository implements OtpRepository {

    private static final String INSERT_OTP_CODE = "INSERT INTO otp_codes (user_id, operation_id, code, status, created_at, expires_at) VALUES (?, ?, ?, ?, NOW(), ?)";
    private static final String SELECT_OTP_CODE = "SELECT id, user_id, operation_id, code, status, created_at, expires_at FROM otp_codes WHERE user_id = ? AND operation_id = ? AND code = ?";
    private static final String DELETE_ACTIVE_OTP_CODES = "DELETE FROM otp_codes WHERE status = 'ACTIVE' AND expires_at < ?";
    private static final String DELETE_OTP_CODES_BY_USER_ID = "DELETE FROM otp_codes WHERE user_id = ?";
    private static final String UPDATE_OTP_STATUS_BY_ID = "UPDATE otp_codes SET status = ? WHERE id = ?";

    private static final ResultSetMapper<OtpCode> OTP_CODE_RESULT_SET_MAPPER = rs -> {
        try {
            return new OtpCode(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("operation_id"),
                rs.getString("code"),
                OtpStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("expires_at").toLocalDateTime()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    @Override
    public boolean save(long userId, String operationId, String code, OtpStatus status, Instant expiresAt) {
        log.debug("Saving OTP code for user_id: {}, operation_id: {}, code: {}, status: {}, expires_at: {}",
            userId, operationId, code, status, expiresAt);

        return Queries.update(INSERT_OTP_CODE, userId, operationId, code, status.name(), Timestamp.from(expiresAt)) > 0;
    }

    @Override
    public Optional<OtpCode> findByUserIdAndOperationIdAndCode(long userId, String operationId, String code) {
        log.debug("Looking for OTP code with user_id: {}, operation_id: {}, code: {}", userId, operationId, code);

        return Queries.query(SELECT_OTP_CODE, OTP_CODE_RESULT_SET_MAPPER, userId, operationId, code).stream().findFirst();
    }

    @Override
    public boolean deleteActive(Instant expiresAt) {
        log.debug("Deleting active OTP codes that have expired before: {}", expiresAt);

        return Queries.update(DELETE_ACTIVE_OTP_CODES, Timestamp.from(expiresAt)) > 0;
    }

    @Override
    public int deleteByUserId(long userId) {
        log.debug("Deleting all OTP codes for user_id: {}", userId);

        return Queries.update(DELETE_OTP_CODES_BY_USER_ID, userId);
    }
    
    @Override
    public boolean updateStatusById(long id, OtpStatus status) {
        log.debug("Updating OTP code status by ID: {}, new status: {}", id, status);
        
        return Queries.update(UPDATE_OTP_STATUS_BY_ID, status.name(), id) > 0;
    }
}