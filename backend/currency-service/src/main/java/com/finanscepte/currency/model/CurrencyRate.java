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
@Document(collection = "currency_rates")
public class CurrencyRate {
    @Id
    private String id;
    private String symbol;
    private String name;
    private double rate;
    private double changePercent24h;
    private double high24h;
    private double low24h;
    private LocalDateTime lastUpdated;
    private String type; // FIAT / CRYPTO
}
