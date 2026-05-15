package com.finanscepte.settings.controller;

import com.finanscepte.common.exception.GlobalExceptionHandler;
import com.finanscepte.settings.model.UserSettings;
import com.finanscepte.settings.repository.UserProfileRepository;
import com.finanscepte.settings.repository.UserSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettingsController.class)
@Import(GlobalExceptionHandler.class)
class SettingsControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserSettingsRepository userSettingsRepository;
    @MockBean private UserProfileRepository userProfileRepository;

    @Test
    void getSettings_shouldReturnExisting() throws Exception {
        UserSettings settings = UserSettings.builder()
                .userId("u1")
                .darkMode(true)
                .language("tr")
                .currency("TRY")
                .build();
        when(userSettingsRepository.findByUserId("u1")).thenReturn(settings);

        mockMvc.perform(get("/api/settings").param("userId", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u1"))
                .andExpect(jsonPath("$.language").value("tr"));
    }

    @Test
    void getSettings_shouldCreateDefault_whenMissing() throws Exception {
        when(userSettingsRepository.findByUserId("u2")).thenReturn(null);
        when(userSettingsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(get("/api/settings").param("userId", "u2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u2"));
    }

    @Test
    void changePassword_shouldReturnBadRequest_whenFieldsMissing() throws Exception {
        mockMvc.perform(post("/api/settings/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
