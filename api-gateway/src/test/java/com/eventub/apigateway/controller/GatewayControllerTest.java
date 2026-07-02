package com.eventub.apigateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayControllerTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void health_returns200AndMessage() {
        webTestClient.get()
                .uri("/api/gateway/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("API Gateway is running");
    }

    @Test
    void routes_returnsAvailableRoutes() {
        webTestClient.get()
                .uri("/api/gateway/routes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(4)
                .jsonPath("$[0]").isEqualTo("user-service")
                .jsonPath("$[1]").isEqualTo("event-service")
                .jsonPath("$[2]").isEqualTo("registration-service")
                .jsonPath("$[3]").isEqualTo("notification-service");
    }
}
