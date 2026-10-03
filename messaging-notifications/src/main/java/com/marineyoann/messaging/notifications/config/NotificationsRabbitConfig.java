package com.marineyoann.messaging.notifications.config;

import org.springframework.amqp.core.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class NotificationsRabbitConfig {

    private static final Logger log = LoggerFactory.getLogger(NotificationsRabbitConfig.class);
    public static final String NOTIFICATIONS_QUEUE = "messaging.notifications";

    @Bean
    public Queue notificationsQueue() {
        return new Queue(NOTIFICATIONS_QUEUE, true);
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange messagingExchange) {
        return BindingBuilder.bind(notificationsQueue).to(messagingExchange).with("MessageCreated");
    }

    @Bean
    public CommandLineRunner declareNotificationsTopology(AmqpAdmin amqpAdmin,
                                                          Queue notificationsQueue,
                                                          Binding notificationsBinding) {
        return args -> {
            try {
                amqpAdmin.declareQueue(notificationsQueue);
                amqpAdmin.declareBinding(notificationsBinding);
            } catch (Exception e) {
                log.warn("Déclaration eager de la topologie notifications impossible au démarrage : {}", e.getMessage());
            }
        };
    }
}