package com.eventub.notificationservice;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class NotificationServiceApplicationTests {

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private RabbitListenerContainerFactory<?> rabbitListenerContainerFactory;

    @Test
    void contextLoads() {
    }

}
