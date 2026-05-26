package com.dfedorino.otp.service;

import com.dfedorino.otp.domain.enums.Role;

public interface JwtService {

    String generateToken(long userId, String username, Role role);

    boolean isTokenValid(String token);

    String extractUsername(String token);

    Role extractRole(String token);

    long extractUserId(String token);
}
