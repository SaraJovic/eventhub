package com.eventub.apigateway.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayE2ETest {

    private static final WireMockServer USER_SERVICE_MOCK =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private static final WireMockServer EVENT_SERVICE_MOCK =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private static final WireMockServer REGISTRATION_SERVICE_MOCK =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private static final WireMockServer NOTIFICATION_SERVICE_MOCK =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeAll
    static void startWireMockServers() {
        USER_SERVICE_MOCK.start();
        EVENT_SERVICE_MOCK.start();
        REGISTRATION_SERVICE_MOCK.start();
        NOTIFICATION_SERVICE_MOCK.start();
    }

    @AfterAll
    static void stopWireMockServers() {
        USER_SERVICE_MOCK.stop();
        EVENT_SERVICE_MOCK.stop();
        REGISTRATION_SERVICE_MOCK.stop();
        NOTIFICATION_SERVICE_MOCK.stop();
    }

    @DynamicPropertySource
    static void overrideDownstreamRoutes(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gateway.server.webflux.routes[0].id", () -> "user-service");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].uri",
                () -> "http://localhost:" + USER_SERVICE_MOCK.port());
        registry.add("spring.cloud.gateway.server.webflux.routes[0].predicates[0]", () -> "Path=/api/users/**");

        registry.add("spring.cloud.gateway.server.webflux.routes[1].id", () -> "event-service");
        registry.add("spring.cloud.gateway.server.webflux.routes[1].uri",
                () -> "http://localhost:" + EVENT_SERVICE_MOCK.port());
        registry.add("spring.cloud.gateway.server.webflux.routes[1].predicates[0]", () -> "Path=/api/events/**");

        registry.add("spring.cloud.gateway.server.webflux.routes[2].id", () -> "registration-service");
        registry.add("spring.cloud.gateway.server.webflux.routes[2].uri",
                () -> "http://localhost:" + REGISTRATION_SERVICE_MOCK.port());
        registry.add("spring.cloud.gateway.server.webflux.routes[2].predicates[0]", () -> "Path=/api/registrations/**");

        registry.add("spring.cloud.gateway.server.webflux.routes[3].id", () -> "notification-service");
        registry.add("spring.cloud.gateway.server.webflux.routes[3].uri",
                () -> "http://localhost:" + NOTIFICATION_SERVICE_MOCK.port());
        registry.add("spring.cloud.gateway.server.webflux.routes[3].predicates[0]", () -> "Path=/api/notifications/**");
    }

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        USER_SERVICE_MOCK.resetAll();
        EVENT_SERVICE_MOCK.resetAll();
        REGISTRATION_SERVICE_MOCK.resetAll();
        NOTIFICATION_SERVICE_MOCK.resetAll();
    }

    @Test
    void routesToUserService_andReturnsStubbedResponseUnmodified() {
        USER_SERVICE_MOCK.stubFor(get(urlEqualTo("/api/users/42"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"id\":42,\"fullName\":\"Stub User\",\"email\":\"stub@example.com\"}")));

        webTestClient.get().uri("/api/users/42")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(42)
                .jsonPath("$.fullName").isEqualTo("Stub User")
                .jsonPath("$.email").isEqualTo("stub@example.com");

        USER_SERVICE_MOCK.verify(getRequestedFor(urlEqualTo("/api/users/42")));
    }

    @Test
    void routesToEventService_andReturnsStubbedResponseUnmodified() {
        EVENT_SERVICE_MOCK.stubFor(get(urlEqualTo("/api/events/7"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"id\":7,\"title\":\"Stub Event\",\"location\":\"Nowhere\"}")));

        webTestClient.get().uri("/api/events/7")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(7)
                .jsonPath("$.title").isEqualTo("Stub Event")
                .jsonPath("$.location").isEqualTo("Nowhere");

        EVENT_SERVICE_MOCK.verify(getRequestedFor(urlEqualTo("/api/events/7")));
    }

    @Test
    void routesToRegistrationService_andReturnsStubbedResponseUnmodified() {
        REGISTRATION_SERVICE_MOCK.stubFor(get(urlEqualTo("/api/registrations/3"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"id\":3,\"userId\":10,\"eventId\":20,\"status\":\"CONFIRMED\"}")));

        webTestClient.get().uri("/api/registrations/3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(3)
                .jsonPath("$.status").isEqualTo("CONFIRMED");

        REGISTRATION_SERVICE_MOCK.verify(getRequestedFor(urlEqualTo("/api/registrations/3")));
    }

    @Test
    void routesToNotificationService_andReturnsStubbedResponseUnmodified() {
        NOTIFICATION_SERVICE_MOCK.stubFor(get(urlEqualTo("/api/notifications/status"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"service\":\"notification-service\",\"status\":\"UP\",\"queue\":\"registration.queue\"}")));

        webTestClient.get().uri("/api/notifications/status")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.service").isEqualTo("notification-service")
                .jsonPath("$.status").isEqualTo("UP");

        NOTIFICATION_SERVICE_MOCK.verify(getRequestedFor(urlEqualTo("/api/notifications/status")));
    }
}
