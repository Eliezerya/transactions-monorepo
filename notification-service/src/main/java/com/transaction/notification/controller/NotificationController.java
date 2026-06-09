package com.transaction.notification.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/notifications")
@EnableAsync
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping
    @Async
    public CompletableFuture<ResponseEntity<?>> sendNotification(@RequestBody Map<String, Object> request) {
        log.info("Received notification request: {}", request);
        
        // Simple implementation for a notification service
        String type = (String) request.get("type");
        String message = (String) request.get("message");
        Object userId = request.get("userId");

        log.info("Asynchronously sending {} notification to user {}: {}", type, userId, message);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Notification processed asynchronously");

        return CompletableFuture.completedFuture(ResponseEntity.ok(response));
    }
}
