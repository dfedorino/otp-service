package com.dfedorino.otp.service;

import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.domain.enums.Role;

public interface AuthService {

    User register(
        String login,
        String phoneNumber,
        String password,
        Role role
    );

    String login(
        String login,
        String password
    );
}