package com.marineyoann.messaging.security.exception;

public class StompAuthorizationException extends RuntimeException {
    public StompAuthorizationException(String message) {
        super(message);
    }
}
