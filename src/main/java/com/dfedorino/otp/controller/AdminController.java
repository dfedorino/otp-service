package com.dfedorino.otp.controller;

import com.dfedorino.otp.controller.auth.annotation.RequiresRole;
import com.dfedorino.otp.controller.dto.ErrorResponse;
import com.dfedorino.otp.controller.dto.UserRequest;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.domain.exception.OtpConfigNotFoundException;
import com.dfedorino.otp.domain.exception.UserNotFoundException;
import com.dfedorino.otp.domain.model.OtpConfig;
import com.dfedorino.otp.service.AdminService;
import com.dfedorino.otp.service.AuthService;
import com.dfedorino.otp.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final AuthService authService;

    @RequiresRole(Role.ADMIN)
    @GetMapping("/users")
    public List<UserDto> getUsers() {
        return adminService.getUsers();
    }

    @RequiresRole(Role.ADMIN)
    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteUser(@RequestParam(name = "userId") Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @RequiresRole(Role.ADMIN)
    @PutMapping("/otp/config")
    public OtpConfig updateOtpConfig(@RequestBody OtpConfig config) {
        return adminService.updateOtpConfig(config);
    }

    @PostMapping("/users")
    public UserDto createUser(@RequestBody UserRequest request) {
        var created = authService.register(request.login(), request.password(), Role.ADMIN);
        return new UserDto(created.id(), created.login(), created.role());
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
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ex.getMessage()));
    }
}