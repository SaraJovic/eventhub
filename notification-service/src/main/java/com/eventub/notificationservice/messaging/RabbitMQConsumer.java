package com.eventub.notificationservice.messaging;

import com.eventub.notificationservice.dto.RegistrationMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConsumer.class);

    @RabbitListener(queues = "${rabbitmq.queue.name:registration.queue}")
    public void consumeRegistrationMessage(RegistrationMessageDTO message) {
        log.info("Received registration notification: userId={}, eventId={}, status={}",
                message.userId(), message.eventId(), message.status());
    }
}
