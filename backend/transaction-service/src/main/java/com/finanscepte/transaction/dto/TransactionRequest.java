package com.finanscepte.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank String userId,
        String productId,
        @NotNull BigDecimal amount,
        @NotBlank String type,
        String description
) {}
