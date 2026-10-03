package com.marineyoann.messaging.notifications.push;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "push_subscription")
public class PushSubscription {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 1000)
    private String endpoint;

    @Column(nullable = false)
    private String p256dhKey;

    @Column(nullable = false)
    private String authKey;

    @Column(nullable = false)
    private Instant createdAt;

    protected PushSubscription() {
    }

    public PushSubscription(UUID userId, String endpoint, String p256dhKey, String authKey) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.endpoint = endpoint;
        this.p256dhKey = p256dhKey;
        this.authKey = authKey;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getEndpoint() { return endpoint; }
    public String getP256dhKey() { return p256dhKey; }
    public String getAuthKey() { return authKey; }
}