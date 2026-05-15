package com.finanscepte.accounts.util;

import com.finanscepte.accounts.dto.AssetRequest;
import com.finanscepte.accounts.dto.AssetResponse;
import com.finanscepte.accounts.model.Asset;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AssetMapper {

    public Asset toEntity(AssetRequest request) {
        return Asset.builder()
                .userId(request.userId())
                .name(request.name())
                .type(Asset.AssetType.valueOf(request.type()))
                .currentValue(request.currentValue())
                .purchaseValue(request.purchaseValue())
                .quantity(request.quantity())
                .currency(request.currency() != null ? request.currency() : "TRY")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getUserId(),
                asset.getName(),
                asset.getType().name(),
                asset.getCurrentValue(),
                asset.getPurchaseValue(),
                asset.getQuantity(),
                asset.getCurrency(),
                asset.getProfitLoss(),
                asset.getTotalValue(),
                asset.getCreatedAt()
        );
    }

    public void updateEntity(Asset asset, AssetRequest request) {
        asset.setName(request.name());
        asset.setType(Asset.AssetType.valueOf(request.type()));
        asset.setCurrentValue(request.currentValue());
        asset.setPurchaseValue(request.purchaseValue());
        asset.setQuantity(request.quantity());
        if (request.currency() != null) asset.setCurrency(request.currency());
    }
}
