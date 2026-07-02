package com.eventub.notificationservice.messaging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.eventub.notificationservice.dto.RegistrationMessageDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConsumerTest {

    private final RabbitMQConsumer consumer = new RabbitMQConsumer();
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(RabbitMQConsumer.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void consumeRegistrationMessage_logsReceivedNotification() {
        RegistrationMessageDTO message = new RegistrationMessageDTO(
                1L, 10L, 20L, "CONFIRMED", LocalDateTime.now());

        consumer.consumeRegistrationMessage(message);

        assertThat(listAppender.list).hasSize(1);
        String formattedMessage = listAppender.list.get(0).getFormattedMessage();
        assertThat(formattedMessage)
                .contains("Received registration notification")
                .contains("userId=10")
                .contains("eventId=20")
                .contains("status=CONFIRMED");
    }
}
