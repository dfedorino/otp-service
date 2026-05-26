package com.dfedorino.otp.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class PasswordUtil {

    private PasswordUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Hashes a raw password using BCrypt with automatic salt generation.
     *
     * @param rawPassword the raw password to hash
     * @return the hashed password
     * @throws IllegalArgumentException if the password is null or blank
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        return new String(BCrypt.withDefaults().hash(BCrypt.MIN_COST, rawPassword.getBytes()));
    }

    /**
     * Verifies a raw password against a hashed password using BCrypt.
     *
     * @param rawPassword the raw password to verify
     * @param hashedPassword the hashed password to compare against
     * @return true if the password matches, false otherwise
     * @throws IllegalArgumentException if either parameter is null
     */
    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        if (hashedPassword == null) {
            throw new IllegalArgumentException("Hashed password must not be null");
        }
        return BCrypt.verifyer().verify(rawPassword.getBytes(), hashedPassword.getBytes()).verified;
    }
}