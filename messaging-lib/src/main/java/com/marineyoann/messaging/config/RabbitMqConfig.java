package com.marineyoann.messaging.config;


import org.springframework.amqp.core.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RabbitMqConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqConfig.class);

    public static final String MESSAGING_EXCHANGE = "messaging.events";
    public static final String STOMP_BRIDGE_QUEUE = "messaging.stomp-bridge";

    @Bean
    public TopicExchange messagingExchange() {
        return new TopicExchange(MESSAGING_EXCHANGE);
    }

    @Bean
    public Queue stompBridgeQueue() {
        return new Queue(STOMP_BRIDGE_QUEUE, true); // durable : survit à un redémarrage de RabbitMQ
    }

    @Bean
    public Binding stompBridgeBinding(Queue stompBridgeQueue, TopicExchange messagingExchange) {
        return BindingBuilder.bind(stompBridgeQueue).to(messagingExchange).with("MessageCreated");
    }

    @Bean
    public CommandLineRunner declareMessagingTopology(AmqpAdmin amqpAdmin,
                                                      TopicExchange messagingExchange,
                                                      Queue stompBridgeQueue,
                                                      Binding stompBridgeBinding) {
        return args -> {
            try {
                amqpAdmin.declareExchange(messagingExchange);
                amqpAdmin.declareQueue(stompBridgeQueue);
                amqpAdmin.declareBinding(stompBridgeBinding);
            } catch (Exception e) {
                // Non bloquant : RabbitAdmin redéclarera automatiquement à la
                // première connexion réelle si le broker n'est pas encore prêt.
                log.warn("Déclaration eager de la topologie RabbitMQ impossible au démarrage, sera retentée à la première connexion : {}", e.getMessage());
            }
        };
    }
}