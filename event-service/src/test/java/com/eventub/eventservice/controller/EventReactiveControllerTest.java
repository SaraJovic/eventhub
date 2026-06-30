package com.eventub.eventservice.controller;

import com.eventub.eventservice.domain.Event;
import com.eventub.eventservice.dto.EventResponse;
import com.eventub.eventservice.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventReactiveControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EventRepository eventRepository;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    void streamEvents_emptyDatabase_completesWithNoElements() {
        StepVerifier.create(
                        webTestClient.get()
                                .uri("/api/events/stream")
                                .accept(MediaType.TEXT_EVENT_STREAM)
                                .exchange()
                                .expectStatus().isOk()
                                .returnResult(EventResponse.class)
                                .getResponseBody()
                )
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void streamEvents_withOneEvent_emitsOneElement() {
        Event event = new Event();
        event.setTitle("Stream Event");
        event.setDescription("Test");
        event.setLocation("Belgrade");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setCapacity(50);
        event.setOrganizerId(1L);
        eventRepository.save(event);

        StepVerifier.create(
                        webTestClient.get()
                                .uri("/api/events/stream")
                                .accept(MediaType.TEXT_EVENT_STREAM)
                                .exchange()
                                .expectStatus().isOk()
                                .returnResult(EventResponse.class)
                                .getResponseBody()
                )
                .assertNext(response -> {
                    assert response.title().equals("Stream Event");
                    assert response.location().equals("Belgrade");
                })
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }
}
