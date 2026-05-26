package com.dfedorino.otp.service;

import com.dfedorino.otp.domain.model.User;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.repository.UserRepository;
import com.dfedorino.otp.util.PasswordUtil;
import com.dfedorino.otp.repository.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final JwtService jwtUtil;
    
    @Override
    @Transactional
    public User register(
        String login,
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
            throw new IllegalStateException("Login already exists");
        }
        
        // If role == ADMIN, check if admin already exists
        if (role == Role.ADMIN && userRepository.existsAdmin()) {
            throw new IllegalStateException("Administrator already exists");
        }
        
        // Hash password
        String hashedPassword = PasswordUtil.hash(password);
        
        // Save user
        boolean saved = userRepository.save(login, hashedPassword, role);
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
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        User user = userOpt.get();
        
        // Verify password using BCrypt
        if (!PasswordUtil.matches(password, user.hashedPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        // Generate JWT
        return jwtUtil.generateToken(
            user.id(),
            user.login(),
            user.role()
        );
    }
}