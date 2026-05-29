package com.dfedorino.otp.service.dto;

import com.dfedorino.otp.domain.enums.Role;

public record UserDto(
    Long id,
    String login,
    String phoneNumber,
    Role role
) {

}
