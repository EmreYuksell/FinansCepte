package com.finanscepte.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanscepte.common.exception.GlobalExceptionHandler;
import com.finanscepte.transaction.dto.TransactionRequest;
import com.finanscepte.transaction.dto.TransactionResponse;
import com.finanscepte.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private TransactionService transactionService;

    @Test
    void findAll_shouldReturnTransactions() throws Exception {
        TransactionResponse r = new TransactionResponse("1", "u1", null, BigDecimal.TEN, "GELIR", "maas", LocalDateTime.now(), null);
        when(transactionService.findAll()).thenReturn(List.of(r));
        mockMvc.perform(get("/api/transactions")).andExpect(status().isOk()).andExpect(jsonPath("$[0].type").value("GELIR"));
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        TransactionRequest req = new TransactionRequest("u1", null, BigDecimal.valueOf(500), "GELIR", "maas");
        TransactionResponse r = new TransactionResponse("1", "u1", null, BigDecimal.valueOf(500), "GELIR", "maas", LocalDateTime.now(), null);
        when(transactionService.create(any())).thenReturn(r);
        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.amount").value(500));
    }

    @Test
    void createInvalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"\",\"amount\":null,\"type\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByUserId_shouldReturnFiltered() throws Exception {
        when(transactionService.findByUserId("u1")).thenReturn(List.of());
        mockMvc.perform(get("/api/transactions/user/u1")).andExpect(status().isOk());
    }

    @Test
    void findByUserIdAndType_shouldReturnFiltered() throws Exception {
        when(transactionService.findByUserIdAndType("u1", "GELIR")).thenReturn(List.of());
        mockMvc.perform(get("/api/transactions/user/u1?type=GELIR")).andExpect(status().isOk());
    }

    @Test
    void update_shouldReturnUpdated() throws Exception {
        TransactionRequest req = new TransactionRequest("u1", null, BigDecimal.ONE, "GIDER", "harcama");
        when(transactionService.update(eq("1"), any())).thenReturn(new TransactionResponse("1", "u1", null, BigDecimal.ONE, "GIDER", "harcama", LocalDateTime.now(), null));
        mockMvc.perform(put("/api/transactions/1").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/transactions/1")).andExpect(status().isNoContent());
    }
}
