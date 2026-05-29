package com.dfedorino.otp.delivery.impl;

import com.dfedorino.otp.delivery.DeliveryChannel;
import com.dfedorino.otp.service.dto.OtpCodeDto;
import com.dfedorino.otp.service.dto.UserDto;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class FileDeliveryChannel implements DeliveryChannel {

    private final String filePath;
    private final DateTimeFormatter formatter;

    public FileDeliveryChannel(String filePath) {
        // Load configuration
        this.filePath = filePath;
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // Validate configuration
        if (filePath == null || filePath.isEmpty()) {
            log.warn("File path is not configured, using default 'otp-codes.txt'");
        }
    }

    @Override
    public void deliver(UserDto user, OtpCodeDto otp) {
        if (user == null) {
            log.warn("Cannot write OTP - invalid user");
            return;
        }
        if (otp == null || otp.code() == null) {
            log.warn("Cannot write OTP - invalid OTP code");
            return;
        }
        
        writeCodeToFile(user, otp);
    }

    private void writeCodeToFile(UserDto user, OtpCodeDto otp) {
        try {
            Path path = Paths.get(filePath);

            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            
            // Format the message
            String username = user.login() != null ? user.login() : 
                              user.phoneNumber() != null ? user.phoneNumber() : 
                              "unknown";
            String message = String.format("[%s] User: %s, Code: %s%n",
                formatter.format(LocalDateTime.now()),
                username,
                otp.code());
            
            // Append to file
            Files.writeString(
                path,
                message,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
            
            log.info("Successfully wrote OTP code to file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to write OTP code to file: {}", filePath, e);
            throw new RuntimeException("Failed to write OTP code to file", e);
        } catch (Exception e) {
            log.error("Unexpected error writing OTP code to file: {}", filePath, e);
            throw new RuntimeException("Unexpected error writing OTP code to file", e);
        }
    }

    @Override
    public String name() {
        return "FILE";
    }
}