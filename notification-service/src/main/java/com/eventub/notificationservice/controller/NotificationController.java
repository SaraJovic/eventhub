package com.eventub.notificationservice.controller;

import com.eventub.notificationservice.config.RabbitMQConfig;
import com.eventub.notificationservice.dto.NotificationStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification service is running");
    }

    @GetMapping("/status")
    public ResponseEntity<NotificationStatusResponse> status() {
        return ResponseEntity.ok(new NotificationStatusResponse(
                "notification-service", "UP", RabbitMQConfig.QUEUE_NAME));
    }
}
