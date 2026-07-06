package com.eventub.registrationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "registration.queue";
    public static final String EXCHANGE_NAME = "registration.exchange";
    public static final String ROUTING_KEY = "registration.key";

    @Bean
    public Queue registrationQueue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public DirectExchange registrationExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding registrationBinding(Queue registrationQueue, DirectExchange registrationExchange) {
        return BindingBuilder.bind(registrationQueue).to(registrationExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new SimpleMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
