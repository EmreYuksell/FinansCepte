package com.finanscepte.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        String category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
