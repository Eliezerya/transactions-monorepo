package com.transaction.wallet.service;

import com.transaction.wallet.model.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${AUTH_SERVICE_URL:http://localhost:8080}/api/auth")
interface AuthClient {
    @GetMapping("/validate")
    UserDto validateToken(@RequestHeader("authorization") String token);
}

@Service
public class AuthService {

    private final AuthClient authClient;

    public AuthService(AuthClient authClient) {
        this.authClient = authClient;
    }

    public UserDto validateToken(String token) {
        try {
            return authClient.validateToken(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Token validation failed: " + e.getMessage());
        }
    }
}
