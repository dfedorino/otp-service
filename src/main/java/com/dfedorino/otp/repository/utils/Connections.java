package com.dfedorino.otp.repository.utils;

import com.dfedorino.otp.domain.exception.ConnectionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Connections {

    public static Connection initConnection() {
        try {

            return DriverManager.getConnection(
                System.getProperty("DB_URL", System.getenv("DB_URL")),
                System.getProperty("DB_USER", System.getenv("DB_USER")),
                System.getProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"))
            );
        } catch (SQLException e) {
            log.error("failed to init connection", e);
            throw new ConnectionException("Failed to init connection", e);
        }
    }

    public static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("ConnectionUtils[closeQuietly] Failed to close connection", e);
            }
        }
    }

    public static void restoreOldAutoCommit(
        @NonNull Connection conn,
        @NonNull Boolean oldAutoCommit
    ) {
        try {
            conn.setAutoCommit(oldAutoCommit);
        } catch (SQLException e) {
            log.error("ConnectionUtils[restoreOldAutoCommit] failed to execute callback", e);
            throw new ConnectionException("Failed to restore old autocommit", e);
        }
    }

    public static void rollback(@NonNull Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.error("ConnectionUtils[rollback] failed to rollback", e);
            throw new ConnectionException("Rollback failed", e);
        }
    }

}
