package com.dfedorino.otp.delivery;

import com.dfedorino.otp.service.dto.OtpCodeDto;
import com.dfedorino.otp.service.dto.UserDto;

public interface DeliveryChannel {

    void deliver(UserDto user, OtpCodeDto otp);

    String name();
}