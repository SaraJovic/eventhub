package com.eventub.userservice.controller;

import com.eventub.userservice.domain.User;
import com.eventub.userservice.dto.UserResponse;
import com.eventub.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserReactiveControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    void streamUsers_emptyDatabase_completesWithNoElements() {
        StepVerifier.create(
                        webTestClient.get()
                                .uri("/api/users/stream")
                                .accept(MediaType.TEXT_EVENT_STREAM)
                                .exchange()
                                .expectStatus().isOk()
                                .returnResult(UserResponse.class)
                                .getResponseBody()
                )
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void streamUsers_withOneUser_emitsOneElement() {
        User user = new User();
        user.setFullName("Stream User");
        user.setEmail("stream@example.com");
        userRepository.save(user);

        StepVerifier.create(
                        webTestClient.get()
                                .uri("/api/users/stream")
                                .accept(MediaType.TEXT_EVENT_STREAM)
                                .exchange()
                                .expectStatus().isOk()
                                .returnResult(UserResponse.class)
                                .getResponseBody()
                )
                .assertNext(response -> {
                    assert response.email().equals("stream@example.com");
                    assert response.fullName().equals("Stream User");
                })
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }
}
