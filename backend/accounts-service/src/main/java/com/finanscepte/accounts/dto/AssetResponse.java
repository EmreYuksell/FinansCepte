package com.finanscepte.accounts.dto;

import java.time.LocalDateTime;

public record AssetResponse(
        String id,
        String userId,
        String name,
        String type,
        double currentValue,
        double purchaseValue,
        double quantity,
        String currency,
        double profitLoss,
        double totalValue,
        LocalDateTime createdAt
) {}
