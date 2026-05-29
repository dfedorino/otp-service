package com.dfedorino.otp.controller;

import com.dfedorino.otp.controller.auth.annotation.RequiresRole;
import com.dfedorino.otp.controller.dto.ErrorResponse;
import com.dfedorino.otp.controller.dto.UpdateOtpConfigRequest;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.exception.OtpConfigNotFoundException;
import com.dfedorino.otp.domain.exception.UserNotFoundException;
import com.dfedorino.otp.domain.model.OtpConfig;
import com.dfedorino.otp.service.AdminService;
import com.dfedorino.otp.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @RequiresRole(Role.ADMIN)
    @GetMapping("/users")
    public List<UserDto> getUsers() {
        log.info("Fetching all users");
        List<UserDto> users = adminService.getUsers();
        log.info("Successfully fetched {} users", users.size());
        return users;
    }

    @RequiresRole(Role.ADMIN)
    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteUser(@RequestParam(name = "userId") Long userId) {
        log.info("Deleting user with ID: {}", userId);
        adminService.deleteUser(userId);
        log.info("Successfully deleted user with ID: {}", userId);
        return ResponseEntity.noContent().build();
    }

    @RequiresRole(Role.ADMIN)
    @PutMapping("/otp/config")
    public OtpConfig updateOtpConfig(@RequestBody UpdateOtpConfigRequest updateOtpConfigRequest) {
        log.info("Updating OTP config with values: {}", updateOtpConfigRequest);
        OtpConfig config = adminService.updateOtpConfig(updateOtpConfigRequest);
        log.info("Successfully updated OTP config");
        return config;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
        UserNotFoundException ex
    ) {
        log.error("User not found", ex);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(OtpConfigNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOtpConfigNotFoundException(
        OtpConfigNotFoundException ex
    ) {
        log.error("OTP config not found", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ex.getMessage()));
    }
}