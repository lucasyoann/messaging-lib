package com.marineyoann.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "messaging")
public class MessagingProperties {

    private String endpoint = "/ws";
    private String[] allowedOrigins = {"http://127.0.0.1:8081"};
    private final Relay relay = new Relay();

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public Relay getRelay() {
        return relay;
    }

    public static class Relay {
        // Pas de valeur par défaut ici : null signifie "reprendre spring.rabbitmq.*"
        private String host;
        private String login;
        private String passcode;
        private int port = 61613; // le seul qui a vraiment SA propre valeur par défaut

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPasscode() {
            return passcode;
        }

        public void setPasscode(String passcode) {
            this.passcode = passcode;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
