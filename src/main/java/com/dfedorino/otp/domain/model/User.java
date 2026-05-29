package com.dfedorino.otp.domain.model;

import com.dfedorino.otp.domain.enums.Role;

public record User(
    Long id,
    String login,
    String phoneNumber,
    String hashedPassword,
    Role role
) {

}
