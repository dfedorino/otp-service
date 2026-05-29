package com.dfedorino.otp.common;

import com.dfedorino.otp.controller.dto.UserRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestData {

    public static final UserRequest USER_REQUEST = new UserRequest("testuser", "+79111234567",
        "password123");

    public static final UserRequest ADMIN_REQUEST = new UserRequest("admin", "+79111234567",
        "admin");
}
