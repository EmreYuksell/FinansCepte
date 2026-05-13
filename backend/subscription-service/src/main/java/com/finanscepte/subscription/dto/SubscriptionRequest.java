package com.finanscepte.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionRequest(
        @NotBlank String userId,
        @NotBlank String productId,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate,
        @NotNull BigDecimal amount
) {}
