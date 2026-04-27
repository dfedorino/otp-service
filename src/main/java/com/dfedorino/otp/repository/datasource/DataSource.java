package com.dfedorino.otp.repository.datasource;

import java.sql.Connection;

public interface DataSource {

    Connection getConnection();

    void close();
}
