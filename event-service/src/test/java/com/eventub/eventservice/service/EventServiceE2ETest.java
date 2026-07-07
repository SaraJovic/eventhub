package com.eventub.eventservice.service;

import com.eventub.eventservice.dto.EventCreateRequest;
import com.eventub.eventservice.dto.EventResponse;
import com.eventub.eventservice.dto.EventUpdateRequest;
import com.eventub.eventservice.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class EventServiceE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void fullEventLifecycle_createReadListUpdateDeleteRead() {
        EventCreateRequest createRequest = new EventCreateRequest(
                "Spring Conference", "A conference about Spring", "Belgrade",
                LocalDateTime.now().plusDays(30), 100, 1L);

        ResponseEntity<EventResponse> createResponse =
                restTemplate.postForEntity("/api/events", createRequest, EventResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        EventResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("Spring Conference");
        assertThat(created.location()).isEqualTo("Belgrade");
        assertThat(created.capacity()).isEqualTo(100);
        Long id = created.id();

        ResponseEntity<EventResponse> getResponse =
                restTemplate.getForEntity("/api/events/{id}", EventResponse.class, id);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().title()).isEqualTo("Spring Conference");

        ResponseEntity<EventResponse[]> listResponse =
                restTemplate.getForEntity("/api/events", EventResponse[].class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<EventResponse> events = List.of(listResponse.getBody());
        assertThat(events).extracting(EventResponse::id).contains(id);

        EventUpdateRequest updateRequest = new EventUpdateRequest(
                "Spring Conference Updated", "Updated description", "Novi Sad",
                LocalDateTime.now().plusDays(45), 200, 1L);
        ResponseEntity<EventResponse> updateResponse = restTemplate.exchange(
                "/api/events/{id}", HttpMethod.PUT, new HttpEntity<>(updateRequest), EventResponse.class, id);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().title()).isEqualTo("Spring Conference Updated");
        assertThat(updateResponse.getBody().location()).isEqualTo("Novi Sad");
        assertThat(updateResponse.getBody().capacity()).isEqualTo(200);

        restTemplate.delete("/api/events/{id}", id);

        ResponseEntity<String> afterDeleteResponse =
                restTemplate.getForEntity("/api/events/{id}", String.class, id);

        assertThat(afterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
