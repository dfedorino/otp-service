package com.dfedorino.otp.delivery.impl;

import com.dfedorino.otp.delivery.DeliveryChannel;
import com.dfedorino.otp.service.dto.OtpCodeDto;
import com.dfedorino.otp.service.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
public class SmsDeliveryChannel implements DeliveryChannel {

    public static final String PREFIX = "Your code: ";
    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;
    
    public SmsDeliveryChannel(Properties properties) {
        this.host = properties.getProperty("smpp.host", "localhost");
        this.port = Integer.parseInt(properties.getProperty("smpp.port", "2775"));
        this.systemId = properties.getProperty("smpp.system_id", "smppclient1");
        this.password = properties.getProperty("smpp.password", "password");
        this.systemType = properties.getProperty("smpp.system_type", "OTP");
        this.sourceAddress = properties.getProperty("smpp.source_addr", "OTPService");
        
        // Validate configuration
        if (host == null || host.isEmpty()) {
            log.warn("SMPP host is not configured");
        }
        if (systemId == null || systemId.isEmpty()) {
            log.warn("SMPP system ID is not configured");
        }
        if (password == null || password.isEmpty()) {
            log.warn("SMPP password is not configured");
        }
    }
    
    @Override
    public void deliver(UserDto user, OtpCodeDto otp) {
        if (user == null || user.phoneNumber() == null || user.phoneNumber().isEmpty()) {
            log.warn("Cannot send OTP - invalid user or phone number");
            return;
        }
        if (otp == null || otp.code() == null) {
            log.warn("Cannot send OTP - invalid OTP code");
            return;
        }
        sendCode(user.phoneNumber(), otp.code());
    }
    
    private void sendCode(String destination, String code) {
        SMPPSession session = new SMPPSession();
        
        try {
            BindParameter bindParameter = new BindParameter(
                BindType.BIND_TX,
                systemId,
                password,
                systemType,
                TypeOfNumber.UNKNOWN,
                NumberingPlanIndicator.UNKNOWN,
                sourceAddress
            );
            
            session.connectAndBind(host, port, bindParameter);
            
            session.submitShortMessage(
                systemType,
                TypeOfNumber.UNKNOWN,
                NumberingPlanIndicator.UNKNOWN,
                sourceAddress,
                TypeOfNumber.UNKNOWN,
                NumberingPlanIndicator.UNKNOWN,
                destination,
                new ESMClass(),
                (byte) 0,
                (byte) 1,
                null,
                null,
                new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                (byte) 0,
                new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                (byte) 0,
                (PREFIX + code).getBytes(StandardCharsets.UTF_8)
            );
            
            log.info(">> SMS delivery channel sent to {}", destination);
            
        } catch (Exception e) {
            log.error(">> SMS delivery channel sent to {} failed", destination);
            log.error(">> ", e);
            throw new RuntimeException("Failed to send SMS via SMPP", e);
        } finally {
            try {
                session.unbindAndClose();
            } catch (Exception e) {
                log.warn("Failed to close SMPP session", e);
            }
        }
    }
    
    @Override
    public String name() {
        return "SMS";
    }
}