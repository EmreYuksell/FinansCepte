package com.finanscepte.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanscepte.common.exception.GlobalExceptionHandler;
import com.finanscepte.budget.dto.BudgetRequest;
import com.finanscepte.budget.dto.BudgetResponse;
import com.finanscepte.budget.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@Import(GlobalExceptionHandler.class)
class BudgetControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private BudgetService budgetService;

    @Test
    void create_shouldReturnCreated() throws Exception {
        BudgetRequest req = new BudgetRequest("u1", "gida", BigDecimal.valueOf(2000), 5, 2026);
        BudgetResponse resp = new BudgetResponse("1", "u1", "gida", BigDecimal.valueOf(2000), BigDecimal.ZERO, 5, 2026, null, null);
        when(budgetService.create(any())).thenReturn(resp);
        mockMvc.perform(post("/api/budgets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.category").value("gida"));
    }

    @Test
    void findAll_shouldReturnBudgets() throws Exception {
        when(budgetService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/budgets")).andExpect(status().isOk());
    }

    @Test
    void findByUserIdAndPeriod_shouldReturnFiltered() throws Exception {
        when(budgetService.findByUserIdAndMonthAndYear("u1", 5, 2026)).thenReturn(List.of());
        mockMvc.perform(get("/api/budgets/user/u1?month=5&year=2026")).andExpect(status().isOk());
    }

    @Test
    void checkBudgetLimit_shouldReturnBoolean() throws Exception {
        when(budgetService.checkBudgetLimit("u1", "gida", 5, 2026, BigDecimal.valueOf(500))).thenReturn(false);
        mockMvc.perform(get("/api/budgets/check-limit?userId=u1&category=gida&month=5&year=2026&amount=500"))
                .andExpect(status().isOk()).andExpect(content().string("false"));
    }

    @Test
    void createInvalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/budgets").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"\",\"category\":\"\",\"limitAmount\":null,\"month\":null,\"year\":null}"))
                .andExpect(status().isBadRequest());
    }
}
