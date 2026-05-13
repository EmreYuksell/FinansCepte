package com.finanscepte.budget.util;

import com.finanscepte.budget.dto.BudgetRequest;
import com.finanscepte.budget.dto.BudgetResponse;
import com.finanscepte.budget.model.Budget;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class BudgetMapper {

    public Budget toEntity(BudgetRequest request) {
        return Budget.builder()
                .userId(request.userId())
                .category(request.category())
                .limitAmount(request.limitAmount())
                .spentAmount(BigDecimal.ZERO)
                .month(request.month())
                .year(request.year())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getUserId(),
                budget.getCategory(),
                budget.getLimitAmount(),
                budget.getSpentAmount(),
                budget.getMonth(),
                budget.getYear(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
