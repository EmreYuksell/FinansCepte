package com.finanscepte.subscription.util;

import com.finanscepte.subscription.dto.SubscriptionRequest;
import com.finanscepte.subscription.dto.SubscriptionResponse;
import com.finanscepte.subscription.model.Subscription;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SubscriptionMapper {

    public Subscription toEntity(SubscriptionRequest request) {
        return Subscription.builder()
                .userId(request.userId())
                .productId(request.productId())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .amount(request.amount())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getProductId(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getStatus(),
                subscription.getAmount(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
