package com.finanscepte.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String id,
        String userId,
        String productId,
        BigDecimal amount,
        String type,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
