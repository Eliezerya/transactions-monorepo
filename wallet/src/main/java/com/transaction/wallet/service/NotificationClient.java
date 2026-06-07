package com.transaction.wallet.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

// to do : should be async notification
@FeignClient(name = "notification-service", url = "${NOTIFICATION_SERVICE_URL:http://localhost:8082}/api/notifications")
public interface NotificationClient {

    @PostMapping
    ResponseEntity<Map<String, Object>> sendNotification(@RequestBody Map<String, Object> request);
}
