package com.finanscepte.accounts.dto;

import java.time.LocalDateTime;

public record AccountResponse(
        String id,
        String userId,
        String name,
        String type,
        String institution,
        double balance,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
