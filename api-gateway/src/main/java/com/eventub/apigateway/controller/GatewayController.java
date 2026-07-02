package com.eventub.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    private static final List<String> ROUTES = List.of(
            "user-service", "event-service", "registration-service", "notification-service");

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API Gateway is running");
    }

    @GetMapping("/routes")
    public ResponseEntity<List<String>> routes() {
        return ResponseEntity.ok(ROUTES);
    }
}
