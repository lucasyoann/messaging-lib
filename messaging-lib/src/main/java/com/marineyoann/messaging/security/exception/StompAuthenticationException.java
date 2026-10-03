package com.marineyoann.messaging.security.exception;

public class StompAuthenticationException extends RuntimeException {
    public StompAuthenticationException(String message) {
        super(message);
    }
}
