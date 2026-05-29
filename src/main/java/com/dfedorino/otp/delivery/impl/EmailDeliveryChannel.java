package com.dfedorino.otp.delivery.impl;

import com.dfedorino.otp.delivery.DeliveryChannel;
import com.dfedorino.otp.service.dto.OtpCodeDto;
import com.dfedorino.otp.service.dto.UserDto;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class EmailDeliveryChannel implements DeliveryChannel {

    public static final String SUBJECT = "Your OTP Code";
    public static final String PREFIX = "Your verification code is: ";
    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;
    
    public EmailDeliveryChannel(Properties properties) {
        // Load configuration
        this.username = properties.getProperty("email.username");
        this.password = properties.getProperty("email.password");
        this.fromEmail = properties.getProperty("email.from");
        this.session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        
        // Validate configuration
        if (username == null || username.isEmpty()) {
            log.warn("Email username is not configured");
        }
        if (password == null || password.isEmpty()) {
            log.warn("Email password is not configured");
        }
        if (fromEmail == null || fromEmail.isEmpty()) {
            log.warn("Email from address is not configured");
        }
    }

    @Override
    public void deliver(UserDto user, OtpCodeDto otp) {
        if (user == null || user.login() == null || user.login().isEmpty()) {
            log.warn("Cannot send OTP - invalid user or email address");
            return;
        }
        if (otp == null || otp.code() == null) {
            log.warn("Cannot send OTP - invalid OTP code");
            return;
        }
        sendCode(user.login(), otp.code());
    }
    
    private void sendCode(String toEmail, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(SUBJECT);
            message.setText(PREFIX + code);

            Transport.send(message);
            log.info("Successfully sent OTP code to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    @Override
    public String name() {
        return "EMAIL";
    }
}