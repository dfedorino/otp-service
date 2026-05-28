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
    static {
        try {
            Class.forName("org.postgresql.Driver");
            log.debug(">> Driver found");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection initConnection() {
        try {

            String postgresUrl = System.getProperty("POSTGRES_URL", System.getenv("POSTGRES_URL"));
            String postgresUser = System.getProperty("POSTGRES_USER", System.getenv("POSTGRES_USER"));
            String postgresPassword = System.getProperty("POSTGRES_PASSWORD",
                System.getenv("POSTGRES_PASSWORD"));

            return DriverManager.getConnection(
                postgresUrl,
                postgresUser,
                postgresPassword
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
