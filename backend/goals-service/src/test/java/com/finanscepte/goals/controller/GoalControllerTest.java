package com.finanscepte.goals.controller;

import com.finanscepte.common.exception.GlobalExceptionHandler;
import com.finanscepte.goals.dto.GoalRequest;
import com.finanscepte.goals.dto.GoalResponse;
import com.finanscepte.goals.service.GoalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
@Import(GlobalExceptionHandler.class)
class GoalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private GoalService goalService;

    @Test
    void findAll_shouldReturnList() throws Exception {
        GoalResponse resp = new GoalResponse("1", "u1", "Araba", 100000, 10000, 10, LocalDate.now(), "#4f46e5", "tasarruf", LocalDateTime.now());
        when(goalService.findAll()).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Araba"));
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        GoalRequest req = new GoalRequest("u1", "Araba", 100000.0, 10000.0, LocalDate.now().plusMonths(6), "#4f46e5", "tasarruf");
        GoalResponse resp = new GoalResponse("1", "u1", "Araba", 100000, 10000, 10, req.deadline(), "#4f46e5", "tasarruf", LocalDateTime.now());
        when(goalService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Araba"));
    }

    @Test
    void deposit_shouldReturnOk() throws Exception {
        GoalResponse resp = new GoalResponse("1", "u1", "Araba", 100000, 20000, 20, LocalDate.now(), null, null, LocalDateTime.now());
        when(goalService.deposit("1", 100.0)).thenReturn(resp);

        mockMvc.perform(patch("/api/goals/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(20000));
    }
}
