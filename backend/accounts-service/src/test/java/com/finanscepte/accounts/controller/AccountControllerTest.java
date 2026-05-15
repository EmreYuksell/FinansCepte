package com.finanscepte.accounts.controller;

import com.finanscepte.accounts.dto.AccountRequest;
import com.finanscepte.accounts.dto.AccountResponse;
import com.finanscepte.accounts.service.AccountService;
import com.finanscepte.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AccountService accountService;

    @Test
    void findAll_shouldReturnList() throws Exception {
        AccountResponse resp = new AccountResponse("1", "u1", "Vadesiz", "BANK", "Ziraat", 1000, "TRY", LocalDateTime.now(), null);
        when(accountService.findAll()).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Vadesiz"));
    }

    
    @Test
    void update_shouldReturnOk() throws Exception {
        AccountRequest req = new AccountRequest("u1", "Güncel", "BANK", "Ziraat", 500, "TRY");
        AccountResponse resp = new AccountResponse("1", "u1", "Güncel", "BANK", "Ziraat", 500, "TRY", LocalDateTime.now(), null);
        when(accountService.update(eq("1"), any())).thenReturn(resp);

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Güncel"));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void create_shouldReturnCreated() throws Exception {
        AccountRequest req = new AccountRequest("u1", "Vadesiz", "BANK", "Ziraat", 1000, "TRY");
        AccountResponse resp = new AccountResponse("1", "u1", "Vadesiz", "BANK", "Ziraat", 1000, "TRY", LocalDateTime.now(), null);
        when(accountService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("u1"));
    }
}
