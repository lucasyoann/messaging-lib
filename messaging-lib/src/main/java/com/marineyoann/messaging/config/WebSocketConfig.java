package com.marineyoann.messaging.config;

import com.marineyoann.messaging.security.MessagingStompErrorHandler;
import com.marineyoann.messaging.security.StompAuthChannelInterceptor;
import org.springframework.boot.amqp.autoconfigure.RabbitProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final MessagingProperties properties;
    private final RabbitProperties rabbitProperties; // celui de spring.rabbitmq.*
    private final StompAuthChannelInterceptor authInterceptor;

    public WebSocketConfig(MessagingProperties properties,
                           RabbitProperties rabbitProperties,
                           StompAuthChannelInterceptor authInterceptor) {
        this.properties = properties;
        this.rabbitProperties = rabbitProperties;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(properties.getEndpoint())
                .setAllowedOrigins(properties.getAllowedOrigins());
        registry.setErrorHandler(new MessagingStompErrorHandler());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        MessagingProperties.Relay relay = properties.getRelay();

        String host = relay.getHost() != null ? relay.getHost() : rabbitProperties.getHost();
        String login = relay.getLogin() != null ? relay.getLogin() : rabbitProperties.getUsername();
        String passcode = relay.getPasscode() != null ? relay.getPasscode() : rabbitProperties.getPassword();

        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(host)
                .setRelayPort(relay.getPort()) // toujours 61613 par défaut, jamais celui d'AMQP
                .setClientLogin(login)
                .setClientPasscode(passcode);
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}
