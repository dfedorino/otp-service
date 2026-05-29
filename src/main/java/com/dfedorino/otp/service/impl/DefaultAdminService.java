package com.dfedorino.otp.service.impl;

import com.dfedorino.otp.controller.dto.UpdateOtpConfigRequest;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.model.OtpConfig;
import com.dfedorino.otp.repository.OtpConfigRepository;
import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.repository.transaction.Transactional;
import com.dfedorino.otp.service.AdminService;
import com.dfedorino.otp.service.dto.UserDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultAdminService implements AdminService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final OtpConfigRepository otpConfigRepository;

    @Override
    @Transactional
    public List<UserDto> getUsers() {
        return userRepository.findAll()
            .stream()
            .filter(user -> user.role() != Role.ADMIN)
            .map(user -> new UserDto(user.id(), user.login(), user.phoneNumber(), user.role()))
            .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public OtpConfig updateOtpConfig(UpdateOtpConfigRequest updateOtpConfigRequest) {
        // Note: config validation might be added here in practice but not required per task
        otpConfigRepository.update(new OtpConfig(
            1L,
            updateOtpConfigRequest.codeLength(),
            updateOtpConfigRequest.ttlSeconds()
        ));
        return otpConfigRepository.findFirst()
            .orElseThrow();
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        // Check if user exists
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if user is admin
        var user = userOpt.get();
        if (user.role() == Role.ADMIN) {
            throw new IllegalStateException("Cannot delete admin user");
        }

        // Delete OTP codes for user
        otpRepository.deleteByUserId(userId);

        // Delete user
        userRepository.deleteById(userId);
    }
}