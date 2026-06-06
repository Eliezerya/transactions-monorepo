package com.transaction.auth_service.service;

import com.transaction.auth_service.model.Entity.User;
import com.transaction.auth_service.model.Dto.UserDto;
import com.transaction.auth_service.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User registerUser(UserDto request) {
        if (userRepository.existsByUsername(request.getUsername()) || userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Username or Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER"); // Default role

        return userRepository.save(user);
    }

    public Map<String, Object> loginUser(UserDto request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        User user = userOpt.get();
        String token = jwtService.generateToken(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expiresIn", 3600);
        return result;
    }

    public Map<String, Object> validateToken(String token) {
        log.info("Attempting to validate JWT token");
        Claims claims = jwtService.extractClaims(token);

        if (jwtService.isTokenExpired(claims)) {
            log.warn("Token validation failed: Token is expired");
            throw new IllegalArgumentException("JWT Token has expired or is invalid");
        }

        Object userIdVal = claims.get("userId");
        Long userId = 0L;
        if (userIdVal instanceof Number) {
            userId = ((Number) userIdVal).longValue();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("userId", userId);
        result.put("username", claims.getSubject());
        result.put("email", claims.get("email", String.class));
        result.put("role", claims.get("role", String.class));

        log.info("Token validated successfully for user: {}", claims.getSubject());
        return result;
    }
}
