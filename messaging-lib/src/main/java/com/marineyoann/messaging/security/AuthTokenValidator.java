package com.marineyoann.messaging.security;

import java.util.Optional;
import java.util.UUID;

public interface AuthTokenValidator {

    Optional<AuthenticatedUser> validate(String bearerToken);

    record AuthenticatedUser(UUID userId, String displayName) {
    }
}
