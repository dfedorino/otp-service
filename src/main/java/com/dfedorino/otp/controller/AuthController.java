package com.dfedorino.otp.controller;

import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.service.AuthService;
import com.dfedorino.otp.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public record UserRequest(String login, String password) {}

    public record LoginResponse(String token) {}

}
