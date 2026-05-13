package com.finanscepte.subscription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanscepte.common.exception.GlobalExceptionHandler;
import com.finanscepte.subscription.dto.SubscriptionRequest;
import com.finanscepte.subscription.dto.SubscriptionResponse;
import com.finanscepte.subscription.service.SubscriptionService;
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

@WebMvcTest(SubscriptionController.class)
@Import(GlobalExceptionHandler.class)
class SubscriptionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private SubscriptionService subscriptionService;

    @Test
    void create_shouldReturnCreated() throws Exception {
        SubscriptionRequest req = new SubscriptionRequest("u1", "p1", LocalDateTime.now(), LocalDateTime.now().plusMonths(1), BigDecimal.valueOf(99));
        SubscriptionResponse resp = new SubscriptionResponse("1", "u1", "p1", LocalDateTime.now(), LocalDateTime.now().plusMonths(1), "ACTIVE", BigDecimal.valueOf(99), null, null);
        when(subscriptionService.create(any())).thenReturn(resp);
        mockMvc.perform(post("/api/subscriptions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void findAll_shouldReturnList() throws Exception {
        when(subscriptionService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/subscriptions")).andExpect(status().isOk());
    }

    @Test
    void cancel_shouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/subscriptions/1/cancel")).andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/subscriptions/1")).andExpect(status().isNoContent());
    }

    @Test
    void findByUserId_shouldReturnFiltered() throws Exception {
        when(subscriptionService.findByUserId("u1")).thenReturn(List.of());
        mockMvc.perform(get("/api/subscriptions/user/u1")).andExpect(status().isOk());
    }

    @Test
    void createInvalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/subscriptions").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"\",\"productId\":\"\",\"startDate\":null,\"endDate\":null,\"amount\":null}"))
                .andExpect(status().isBadRequest());
    }
}
