package com.finanscepte.subscription.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subscriptions")
public class Subscription {

    @Id
    private String id;

    private String userId;

    private String productId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String status;

    private BigDecimal amount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
