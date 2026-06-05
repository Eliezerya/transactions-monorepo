package com.transaction.auth_service.controller;

import com.transaction.auth_service.model.User;
import com.transaction.auth_service.model.UserDto;
import com.transaction.auth_service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto registrationRequest) {
        if (registrationRequest.getUsername() == null || registrationRequest.getUsername().trim().isEmpty() ||
            registrationRequest.getPassword() == null || registrationRequest.getPassword().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Username and password cannot be empty");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        if (registrationRequest.getEmail() == null || !registrationRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Invalid email format");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        try {
            User user = userService.registerUser(registrationRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty() ||
            loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Invalid username or password");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> tokenInfo = userService.loginUser(loginRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("token", tokenInfo.get("token"));
            response.put("expiresIn", tokenInfo.get("expiresIn"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "JWT Token has expired or is invalid");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            Map<String, Object> validationResult = userService.validateToken(token);
            return ResponseEntity.ok(validationResult);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "JWT Token has expired or is invalid");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}
