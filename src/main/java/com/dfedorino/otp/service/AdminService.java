package com.dfedorino.otp.service;

import com.dfedorino.otp.controller.dto.UpdateOtpConfigRequest;
import com.dfedorino.otp.domain.model.OtpConfig;
import com.dfedorino.otp.service.dto.UserDto;
import java.util.List;

public interface AdminService {

    List<UserDto> getUsers();

    void deleteUser(long userId);

    OtpConfig updateOtpConfig(UpdateOtpConfigRequest updateOtpConfigRequest);
}