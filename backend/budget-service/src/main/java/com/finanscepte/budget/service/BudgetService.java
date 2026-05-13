package com.finanscepte.budget.service;

import com.finanscepte.budget.dto.BudgetRequest;
import com.finanscepte.budget.dto.BudgetResponse;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetService {

    BudgetResponse create(BudgetRequest request);

    BudgetResponse update(String id, BudgetRequest request);

    BudgetResponse findById(String id);

    List<BudgetResponse> findAll();

    List<BudgetResponse> findByUserIdAndMonthAndYear(String userId, Integer month, Integer year);

    List<BudgetResponse> findByUserId(String userId);

    boolean checkBudgetLimit(String userId, String category, Integer month, Integer year, BigDecimal amount);

    void deleteById(String id);
}
