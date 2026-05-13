package com.finanscepte.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        String id,
        String userId,
        String productId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status,
        BigDecimal amount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
