package com.finanscepte.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetResponse(
        String id,
        String userId,
        String category,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        Integer month,
        Integer year,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
