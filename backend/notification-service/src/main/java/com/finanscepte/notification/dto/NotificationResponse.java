package com.finanscepte.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String userId,
        String type,
        String message,
        Boolean read,
        LocalDateTime createdAt
) {}
