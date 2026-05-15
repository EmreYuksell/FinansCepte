package com.finanscepte.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountRequest(
        @NotBlank String userId,
        @NotBlank String name,
        @NotNull String type,
        String institution,
        double balance,
        String currency
) {}
