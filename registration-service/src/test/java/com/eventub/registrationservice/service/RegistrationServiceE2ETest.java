package com.eventub.registrationservice.service;

import com.eventub.registrationservice.config.RabbitMQConfig;
import com.eventub.registrationservice.dto.RegistrationCreateRequest;
import com.eventub.registrationservice.dto.RegistrationMessageDTO;
import com.eventub.registrationservice.dto.RegistrationResponse;
import com.eventub.registrationservice.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(RegistrationServiceE2ETest.E2ETestRabbitListenerConfig.class)
class RegistrationServiceE2ETest {

    private static final String TEST_QUEUE_NAME = "registration.e2e-test.queue";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private CapturedMessages capturedMessages;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        capturedMessages.received.clear();
    }

    @Test
    void createRegistration_persistsInDatabaseAndPublishesRabbitMessage() {
        RegistrationCreateRequest request = new RegistrationCreateRequest(501L, 902L);

        ResponseEntity<RegistrationResponse> createResponse =
                restTemplate.postForEntity("/api/registrations", request, RegistrationResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        RegistrationResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        Long id = created.id();

        ResponseEntity<RegistrationResponse> getResponse =
                restTemplate.getForEntity("/api/registrations/{id}", RegistrationResponse.class, id);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().userId()).isEqualTo(501L);
        assertThat(getResponse.getBody().eventId()).isEqualTo(902L);

        assertThat(registrationRepository.findById(id)).isPresent();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(capturedMessages.received).hasSize(1);
            RegistrationMessageDTO message = capturedMessages.received.get(0);
            assertThat(message.registrationId()).isEqualTo(id);
            assertThat(message.userId()).isEqualTo(501L);
            assertThat(message.eventId()).isEqualTo(902L);
            assertThat(message.status()).isEqualTo("CONFIRMED");
        });
    }

    @TestConfiguration
    static class E2ETestRabbitListenerConfig {

        @Bean
        Queue e2eTestQueue() {
            return new Queue(TEST_QUEUE_NAME, false, false, true);
        }

        @Bean
        Binding e2eTestBinding(Queue e2eTestQueue, DirectExchange registrationExchange) {
            return BindingBuilder.bind(e2eTestQueue).to(registrationExchange).with(RabbitMQConfig.ROUTING_KEY);
        }

        @Bean
        CapturedMessages capturedMessages() {
            return new CapturedMessages();
        }
    }

    static class CapturedMessages {

        private final List<RegistrationMessageDTO> received = new CopyOnWriteArrayList<>();

        @RabbitListener(queues = TEST_QUEUE_NAME)
        void onMessage(RegistrationMessageDTO message) {
            received.add(message);
        }
    }
}
