package com.dfedorino.otp.delivery;

import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.service.dto.OtpCodeDto;

public interface DeliveryChannel {

    void deliver(User user, OtpCodeDto otp);

    String name();
}