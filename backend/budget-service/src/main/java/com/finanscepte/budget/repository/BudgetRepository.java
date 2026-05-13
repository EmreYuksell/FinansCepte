package com.finanscepte.budget.repository;

import com.finanscepte.common.GenericRepository;
import com.finanscepte.budget.model.Budget;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends GenericRepository<Budget, String> {

    List<Budget> findByUserIdAndMonthAndYear(String userId, Integer month, Integer year);

    List<Budget> findByUserId(String userId);
}
