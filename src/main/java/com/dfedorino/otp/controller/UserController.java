package com.dfedorino.otp.controller;

import com.dfedorino.otp.controller.auth.annotation.RequiresRole;
import com.dfedorino.otp.controller.dto.ErrorResponse;
import com.dfedorino.otp.controller.dto.OtpRequest;
import com.dfedorino.otp.controller.dto.ValidateOtpRequest;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.exception.OtpConfigNotFoundException;
import com.dfedorino.otp.domain.exception.UserNotFoundException;
import com.dfedorino.otp.service.UserService;
import com.dfedorino.otp.service.dto.OtpCodeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @RequiresRole(Role.USER)
    @PostMapping("/otp/generate")
    public OtpCodeDto generateOtp(@RequestBody OtpRequest request) {
        return userService.generateOtp(request.userId(), request.operationId());
    }

    @RequiresRole(Role.USER)
    @PostMapping("/otp/validate")
    public boolean validateOtp(@RequestBody ValidateOtpRequest request) {
        return userService.validateOtp(request.userId(), request.operationId(), request.code());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
        UserNotFoundException ex
    ) {
        log.error(">> User not found", ex);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(OtpConfigNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOtpConfigNotFoundException(
        OtpConfigNotFoundException ex
    ) {
        log.error(">> OTP config not found", ex);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
}