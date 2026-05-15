package com.finanscepte.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssetRequest(
        @NotBlank String userId,
        @NotBlank String name,
        @NotNull String type,
        double currentValue,
        double purchaseValue,
        @Positive double quantity,
        String currency
) {}
