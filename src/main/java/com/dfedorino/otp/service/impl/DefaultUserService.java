package com.dfedorino.otp.service.impl;

import com.dfedorino.otp.domain.enums.OtpStatus;
import com.dfedorino.otp.domain.exception.OtpConfigNotFoundException;
import com.dfedorino.otp.domain.exception.UserNotFoundException;
import com.dfedorino.otp.domain.model.*;
import com.dfedorino.otp.delivery.DeliveryChannel;
import com.dfedorino.otp.repository.OtpConfigRepository;
import com.dfedorino.otp.repository.OtpRepository;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.service.UserService;
import com.dfedorino.otp.repository.transaction.Transactional;
import com.dfedorino.otp.service.dto.OtpCodeDto;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

@RequiredArgsConstructor
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final OtpConfigRepository otpConfigRepository;
    private final List<DeliveryChannel> deliveryChannels;

    @Override
    @Transactional
    public OtpCodeDto generateOtp(long userId, String operationId) {
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found, id: " + userId);
        }

        var configOptional = otpConfigRepository.findFirst();
        if (configOptional.isEmpty()) {
            throw new OtpConfigNotFoundException("OTP configuration not found");
        }

        OtpConfig config = configOptional.get();

        String code = RandomStringUtils.insecure().nextNumeric(config.codeLength());

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(config.ttlSeconds());

        otpRepository.save(userId, operationId, code, OtpStatus.ACTIVE, expiresAt.toInstant(
            ZoneOffset.UTC));

        OtpCodeDto otpCodeDto = new OtpCodeDto(userId, operationId, code, OtpStatus.ACTIVE,
            expiresAt);

        deliveryChannels.forEach(
            deliveryChannel -> deliveryChannel.deliver(userOptional.get(), otpCodeDto));

        return otpCodeDto;
    }

    @Override
    @Transactional
    public boolean validateOtp(long userId, String operationId, String code) {
        var otpOptional = otpRepository.findByUserIdAndOperationIdAndCode(userId, operationId,
            code);

        if (otpOptional.isEmpty()) {
            return false;
        }

        OtpCode otp = otpOptional.get();

        if (otp.status() != OtpStatus.ACTIVE) {
            return false;
        }

        if (LocalDateTime.now().isAfter(otp.expiresAt())) {
            otpRepository.updateStatusById(otp.id(), OtpStatus.EXPIRED);
            return false;
        }

        otpRepository.updateStatusById(otp.id(), OtpStatus.USED);
        return true;
    }
}