package com.dfedorino.otp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationPropertiesUtil {
    
    public static Properties loadApplicationProperties() {
        Properties props = new Properties();
        try (InputStream is = ApplicationPropertiesUtil.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
        return props;
    }
}