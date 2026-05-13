package com.finanscepte.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanscepte.common.exception.GlobalExceptionHandler;
import com.finanscepte.notification.dto.NotificationRequest;
import com.finanscepte.notification.dto.NotificationResponse;
import com.finanscepte.notification.service.NotificationService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private NotificationService notificationService;

    @Test
    void create_shouldReturnCreated() throws Exception {
        NotificationRequest req = new NotificationRequest("u1", "INFO", "test mesaj");
        NotificationResponse resp = new NotificationResponse("1", "u1", "INFO", "test mesaj", false, LocalDateTime.now());
        when(notificationService.create(any())).thenReturn(resp);
        mockMvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.message").value("test mesaj"));
    }

    @Test
    void findAll_shouldReturnList() throws Exception {
        when(notificationService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/notifications")).andExpect(status().isOk());
    }

    @Test
    void getUnread_shouldReturnFiltered() throws Exception {
        when(notificationService.getUnreadNotifications("u1")).thenReturn(List.of());
        mockMvc.perform(get("/api/notifications/user/u1/unread")).andExpect(status().isOk());
    }

    @Test
    void markAsRead_shouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/notifications/1/read")).andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/notifications/1")).andExpect(status().isNoContent());
    }
}
