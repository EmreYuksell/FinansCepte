package com.finanscepte.budget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BudgetRequest(
        @NotBlank String userId,
        @NotBlank String category,
        @NotNull BigDecimal limitAmount,
        @NotNull Integer month,
        @NotNull Integer year
) {}
