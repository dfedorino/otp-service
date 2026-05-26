package com.dfedorino.otp.service;

import com.dfedorino.otp.domain.model.OtpConfig;
import com.dfedorino.otp.domain.model.User;
import java.util.List;

public interface AdminService {

    List<User> getUsers();

    void deleteUser(long userId);

    OtpConfig updateOtpConfig(OtpConfig config);
}