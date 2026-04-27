package com.dfedorino.otp.model;

import com.dfedorino.otp.model.enums.Role;

public record User(
    Long id,
    String login,
    String passwordHash,
    Role role
) {

}
