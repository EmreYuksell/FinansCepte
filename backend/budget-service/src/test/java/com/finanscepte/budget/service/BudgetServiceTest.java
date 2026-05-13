package com.finanscepte.budget.service;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.budget.dto.BudgetRequest;
import com.finanscepte.budget.dto.BudgetResponse;
import com.finanscepte.budget.model.Budget;
import com.finanscepte.budget.repository.BudgetRepository;
import com.finanscepte.budget.service.impl.BudgetServiceImpl;
import com.finanscepte.budget.util.BudgetMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock private BudgetRepository repository;
    @Mock private BudgetMapper mapper;
    @InjectMocks private BudgetServiceImpl service;

    @Test
    void create_shouldSaveAndReturn() {
        Budget b = Budget.builder().userId("u1").category("gida").limitAmount(BigDecimal.valueOf(1000)).build();
        BudgetRequest req = new BudgetRequest("u1", "gida", BigDecimal.valueOf(1000), 5, 2026);
        BudgetResponse resp = new BudgetResponse("1", "u1", "gida", BigDecimal.valueOf(1000), BigDecimal.ZERO, 5, 2026, null, null);
        when(mapper.toEntity(req)).thenReturn(b);
        when(repository.save(b)).thenReturn(b);
        when(mapper.toResponse(b)).thenReturn(resp);

        BudgetResponse result = service.create(req);
        assertThat(result.limitAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void checkBudgetLimit_shouldReturnTrue_whenExceeded() {
        Budget b = Budget.builder().userId("u1").category("gida").limitAmount(BigDecimal.valueOf(1000)).spentAmount(BigDecimal.valueOf(800)).build();
        when(repository.findByUserIdAndMonthAndYear("u1", 5, 2026)).thenReturn(List.of(b));

        boolean exceeded = service.checkBudgetLimit("u1", "gida", 5, 2026, BigDecimal.valueOf(300));
        assertThat(exceeded).isTrue();
    }

    @Test
    void checkBudgetLimit_shouldReturnFalse_whenUnderLimit() {
        Budget b = Budget.builder().userId("u1").category("gida").limitAmount(BigDecimal.valueOf(1000)).spentAmount(BigDecimal.valueOf(200)).build();
        when(repository.findByUserIdAndMonthAndYear("u1", 5, 2026)).thenReturn(List.of(b));

        boolean exceeded = service.checkBudgetLimit("u1", "gida", 5, 2026, BigDecimal.valueOf(300));
        assertThat(exceeded).isFalse();
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(repository.findById("99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById("99")).isInstanceOf(ResourceNotFoundException.class);
    }
}
