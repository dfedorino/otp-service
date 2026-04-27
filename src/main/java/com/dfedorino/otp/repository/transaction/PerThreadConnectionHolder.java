package com.dfedorino.otp.repository.transaction;

import java.sql.Connection;

public class PerThreadConnectionHolder {
    private static final ThreadLocal<Connection> HOLDER = new ThreadLocal<>();

    public static void set(Connection conn) {
        HOLDER.set(conn);
    }

    public static Connection get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
