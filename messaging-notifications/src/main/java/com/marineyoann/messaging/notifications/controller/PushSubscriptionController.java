package com.marineyoann.messaging.notifications.controller;

import com.marineyoann.messaging.notifications.push.PushSubscription;
import com.marineyoann.messaging.notifications.push.PushSubscriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/push-subscriptions")
public class PushSubscriptionController {

    private final PushSubscriptionRepository repository;
    private final Function<String, UUID> emailToUserId;

    public PushSubscriptionController(PushSubscriptionRepository repository,
                                      Function<String, UUID> emailToUserId) {
        this.repository = repository;
        this.emailToUserId = emailToUserId;
    }

    @PostMapping
    public ResponseEntity<Void> register(@RequestBody RegisterSubscriptionRequest request, Principal principal) {
        UUID userId = emailToUserId.apply(principal.getName());

        // idempotent : un même endpoint déjà connu (ex: re-souscription
        // après un refresh de page) ne crée pas de doublon
        repository.deleteByEndpoint(request.endpoint());
        repository.save(new PushSubscription(userId, request.endpoint(), request.p256dhKey(), request.authKey()));

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}