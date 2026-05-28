package com.dfedorino.otp.controller;

import com.dfedorino.otp.controller.dto.ErrorResponse;
import com.dfedorino.otp.controller.dto.LoginResponse;
import com.dfedorino.otp.controller.dto.UserRequest;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.exception.InvalidCredentialsException;
import com.dfedorino.otp.domain.exception.LoginAlreadyExists;
import com.dfedorino.otp.domain.exception.UserNotFoundException;
import com.dfedorino.otp.service.AuthService;
import com.dfedorino.otp.service.dto.UserDto;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping
    public UserDto createUser(@RequestBody UserRequest request) {
        var created = authService.register(request.login(), request.password(), Role.USER);
        return new UserDto(created.id(), created.login(), created.role());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody UserRequest request) {
        var token = authService.login(request.login(), request.password());
        return new LoginResponse(token);
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

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
        InvalidCredentialsException ex
    ) {
        log.error(">> Invalid credentials", ex);
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(LoginAlreadyExists.class)
    public ResponseEntity<ErrorResponse> handleLoginAlreadyExists(
        LoginAlreadyExists ex
    ) {
        log.error(">> Login already exists", ex);
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(ex.getMessage()));
    }

}
