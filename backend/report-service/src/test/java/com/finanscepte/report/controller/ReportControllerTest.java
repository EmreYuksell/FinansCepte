package com.finanscepte.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanscepte.common.exception.GlobalExceptionHandler;
import com.finanscepte.report.dto.ReportRequest;
import com.finanscepte.report.dto.ReportResponse;
import com.finanscepte.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(GlobalExceptionHandler.class)
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ReportService reportService;

    @Test
    void generate_shouldReturnCreated() throws Exception {
        ReportRequest req = new ReportRequest("u1", "MONTHLY_SUMMARY", LocalDateTime.now().minusMonths(1), LocalDateTime.now());
        ReportResponse resp = new ReportResponse("1", "u1", "MONTHLY_SUMMARY", LocalDateTime.now().minusMonths(1), LocalDateTime.now(), Map.of("key", "val"), null);
        when(reportService.generate(any())).thenReturn(resp);
        mockMvc.perform(post("/api/reports").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.type").value("MONTHLY_SUMMARY"));
    }

    @Test
    void findAll_shouldReturnList() throws Exception {
        when(reportService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/reports")).andExpect(status().isOk());
    }

    @Test
    void findByUserId_shouldReturnFiltered() throws Exception {
        when(reportService.findByUserId("u1")).thenReturn(List.of());
        mockMvc.perform(get("/api/reports/user/u1")).andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/reports/1")).andExpect(status().isNoContent());
    }
}
