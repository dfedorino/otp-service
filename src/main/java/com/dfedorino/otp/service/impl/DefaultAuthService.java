package com.dfedorino.otp.service.impl;

import com.dfedorino.otp.domain.exception.AdminAlreadyExists;
import com.dfedorino.otp.domain.exception.InvalidCredentialsException;
import com.dfedorino.otp.domain.exception.LoginAlreadyExists;
import com.dfedorino.otp.domain.exception.UserNotFoundException;
import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.service.AuthService;
import com.dfedorino.otp.util.PasswordUtil;
import com.dfedorino.otp.repository.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultAuthService implements AuthService {
    
    private final UserRepository userRepository;
    private final DefaultJwtService jwtUtil;
    
    @Override
    @Transactional
    public User register(
        String login,
        String phoneNumber,
        String password,
        Role role
    ) {
        // Validate login
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Login cannot be null or empty");
        }

        // Validate password
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // Check for duplicate logins
        Optional<User> existingUser = userRepository.findByLogin(login);
        if (existingUser.isPresent()) {
            throw new LoginAlreadyExists("Login already exists");
        }

        // If role == ADMIN, check if admin already exists
        if (role == Role.ADMIN && userRepository.existsAdmin()) {
            throw new AdminAlreadyExists("Administrator already exists");
        }

        // Hash password
        String hashedPassword = PasswordUtil.hash(password);

        // Save user
        boolean saved = userRepository.save(login, phoneNumber, hashedPassword, role);
        if (!saved) {
            throw new IllegalStateException("Failed to save user");
        }

        // Retrieve persisted user
        Optional<User> persistedUser = userRepository.findByLogin(login);
        if (persistedUser.isEmpty()) {
            throw new IllegalStateException("Failed to retrieve saved user");
        }

        return persistedUser.get();
    }
    
    @Override
    @Transactional
    public String login(
        String login,
        String password
    ) {
        // Find user by login
        Optional<User> userOpt = userRepository.findByLogin(login);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found by login: \"" + login + "\"");
        }
        
        User user = userOpt.get();
        
        // Verify password using BCrypt
        if (!PasswordUtil.matches(password, user.hashedPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        // Generate JWT
        return jwtUtil.generateToken(
            user.id(),
            user.login(),
            user.role()
        );
    }
}