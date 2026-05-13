package com.finanscepte.budget.model;

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
@Document(collection = "budgets")
public class Budget {

    @Id
    private String id;

    private String userId;

    private String category;

    private BigDecimal limitAmount;

    private BigDecimal spentAmount;

    private Integer month;

    private Integer year;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
