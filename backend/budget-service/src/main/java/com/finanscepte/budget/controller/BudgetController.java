package com.finanscepte.budget.controller;

import com.finanscepte.budget.dto.BudgetRequest;
import com.finanscepte.budget.dto.BudgetResponse;
import com.finanscepte.budget.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> findAll() {
        return ResponseEntity.ok(budgetService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(budgetService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BudgetResponse>> findByUserIdAndPeriod(
            @PathVariable String userId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(budgetService.findByUserIdAndMonthAndYear(userId, month, year));
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<BudgetResponse>> findByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(budgetService.findByUserId(userId));
    }

    @GetMapping("/check-limit")
    public ResponseEntity<Boolean> checkBudgetLimit(
            @RequestParam String userId,
            @RequestParam String category,
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(budgetService.checkBudgetLimit(userId, category, month, year, amount));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(@PathVariable String id, @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        budgetService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
