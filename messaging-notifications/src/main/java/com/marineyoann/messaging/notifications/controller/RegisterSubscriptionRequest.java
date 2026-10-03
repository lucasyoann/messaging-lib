package com.marineyoann.messaging.notifications.controller;

import java.util.UUID;

public record RegisterSubscriptionRequest(String endpoint, String p256dhKey, String authKey) {
}