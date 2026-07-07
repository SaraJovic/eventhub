package com.eventub.notificationservice.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.eventub.notificationservice.dto.RegistrationMessageDTO;
import com.eventub.notificationservice.messaging.RabbitMQConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificationServiceE2ETest {

    private static final String ROUTING_KEY = "registration.key";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DirectExchange registrationExchange;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger consumerLogger;

    @BeforeEach
    void setUp() {
        consumerLogger = (Logger) LoggerFactory.getLogger(RabbitMQConsumer.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        consumerLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        consumerLogger.detachAppender(listAppender);
    }

    @Test
    void publishingRegistrationMessage_isConsumedAndLoggedByNotificationService() {
        RegistrationMessageDTO message = new RegistrationMessageDTO(
                77L, 501L, 902L, "CONFIRMED", LocalDateTime.now());

        rabbitTemplate.convertAndSend(registrationExchange.getName(), ROUTING_KEY, message);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(listAppender.list).isNotEmpty();
            String formattedMessage = listAppender.list.get(listAppender.list.size() - 1).getFormattedMessage();
            assertThat(formattedMessage)
                    .contains("Received registration notification")
                    .contains("userId=501")
                    .contains("eventId=902")
                    .contains("status=CONFIRMED");
        });
    }
}
