package com.finanscepte.currency.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "price_alerts")
public class PriceAlert {
    @Id
    private String id;
    private String userId;
    private String symbol;
    private double targetPrice;
    private String condition; // ABOVE / BELOW
    private boolean isActive;
    private LocalDateTime createdAt;
}
