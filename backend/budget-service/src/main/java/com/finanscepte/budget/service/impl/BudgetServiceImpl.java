package com.finanscepte.budget.service.impl;

import com.finanscepte.budget.dto.BudgetRequest;
import com.finanscepte.budget.dto.BudgetResponse;
import com.finanscepte.budget.model.Budget;
import com.finanscepte.budget.repository.BudgetRepository;
import com.finanscepte.budget.service.BudgetService;
import com.finanscepte.budget.util.BudgetMapper;
import com.finanscepte.common.AbstractGenericDtoService;
import com.finanscepte.common.GenericRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BudgetServiceImpl extends AbstractGenericDtoService<BudgetRequest, BudgetResponse, Budget, String>
        implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    public BudgetServiceImpl(BudgetRepository budgetRepository, BudgetMapper budgetMapper) {
        this.budgetRepository = budgetRepository;
        this.budgetMapper = budgetMapper;
    }

    @Override
    protected GenericRepository<Budget, String> getRepository() {
        return budgetRepository;
    }

    @Override
    protected String getEntityName() {
        return "Budget";
    }

    @Override
    protected Budget toEntity(BudgetRequest request) {
        return budgetMapper.toEntity(request);
    }

    @Override
    protected BudgetResponse toResponse(Budget entity) {
        return budgetMapper.toResponse(entity);
    }

    @Override
    protected void applyUpdate(Budget entity, BudgetRequest request) {
        entity.setCategory(request.category());
        entity.setLimitAmount(request.limitAmount());
        entity.setMonth(request.month());
        entity.setYear(request.year());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public List<BudgetResponse> findByUserIdAndMonthAndYear(String userId, Integer month, Integer year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<BudgetResponse> findByUserId(String userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public boolean checkBudgetLimit(String userId, String category, Integer month, Integer year, BigDecimal amount) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        return budgets.stream()
                .filter(b -> b.getCategory().equalsIgnoreCase(category))
                .anyMatch(b -> b.getSpentAmount().add(amount).compareTo(b.getLimitAmount()) > 0);
    }
}
