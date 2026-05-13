package com.finanscepte.budget.service.impl;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.budget.dto.BudgetRequest;
import com.finanscepte.budget.dto.BudgetResponse;
import com.finanscepte.budget.model.Budget;
import com.finanscepte.budget.repository.BudgetRepository;
import com.finanscepte.budget.service.BudgetService;
import com.finanscepte.budget.util.BudgetMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    public BudgetServiceImpl(BudgetRepository budgetRepository, BudgetMapper budgetMapper) {
        this.budgetRepository = budgetRepository;
        this.budgetMapper = budgetMapper;
    }

    @Override
    public BudgetResponse create(BudgetRequest request) {
        Budget budget = budgetMapper.toEntity(request);
        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toResponse(saved);
    }

    @Override
    public BudgetResponse update(String id, BudgetRequest request) {
        Budget existing = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        existing.setCategory(request.category());
        existing.setLimitAmount(request.limitAmount());
        existing.setMonth(request.month());
        existing.setYear(request.year());
        existing.setUpdatedAt(LocalDateTime.now());
        Budget updated = budgetRepository.save(existing);
        return budgetMapper.toResponse(updated);
    }

    @Override
    public BudgetResponse findById(String id) {
        return budgetRepository.findById(id)
                .map(budgetMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
    }

    @Override
    public List<BudgetResponse> findAll() {
        return budgetRepository.findAll().stream()
                .map(budgetMapper::toResponse)
                .toList();
    }

    @Override
    public List<BudgetResponse> findByUserIdAndMonthAndYear(String userId, Integer month, Integer year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year).stream()
                .map(budgetMapper::toResponse)
                .toList();
    }

    @Override
    public List<BudgetResponse> findByUserId(String userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(budgetMapper::toResponse)
                .toList();
    }

    @Override
    public boolean checkBudgetLimit(String userId, String category, Integer month, Integer year, BigDecimal amount) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        return budgets.stream()
                .filter(b -> b.getCategory().equalsIgnoreCase(category))
                .anyMatch(b -> b.getSpentAmount().add(amount).compareTo(b.getLimitAmount()) > 0);
    }

    @Override
    public void deleteById(String id) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget", "id", id);
        }
        budgetRepository.deleteById(id);
    }
}
