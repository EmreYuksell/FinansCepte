package com.finanscepte.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanscepte.common.exception.GlobalExceptionHandler;
import com.finanscepte.common.exception.UnauthorizedException;
import com.finanscepte.user.dto.LoginRequest;
import com.finanscepte.user.dto.UserRequest;
import com.finanscepte.user.dto.UserResponse;
import com.finanscepte.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UserService userService;

    @Test
    void create_shouldReturnCreated() throws Exception {
        UserRequest req = new UserRequest("Test", "test@mail.com", "123456");
        UserResponse resp = new UserResponse("1", "Test", "test@mail.com", LocalDateTime.now(), LocalDateTime.now());
        when(userService.createUser(any())).thenReturn(resp);
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void findAll_shouldReturnList() throws Exception {
        when(userService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/users")).andExpect(status().isOk());
    }

    @Test
    void findById_shouldReturnUser() throws Exception {
        UserResponse resp = new UserResponse("1", "Test", "test@mail.com", null, null);
        when(userService.findById("1")).thenReturn(Optional.of(resp));
        mockMvc.perform(get("/api/users/1")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void findById_shouldReturn404_whenNotFound() throws Exception {
        when(userService.findById("99")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/users/99")).andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1")).andExpect(status().isNoContent());
    }

    @Test
    void createInvalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"email\":\"invalid\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn200_whenValid() throws Exception {
        LoginRequest req = new LoginRequest("test@mail.com", "123456");
        UserResponse resp = new UserResponse("1", "Test", "test@mail.com", LocalDateTime.now(), LocalDateTime.now());
        when(userService.login(any())).thenReturn(resp);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void login_shouldReturn401_whenInvalid() throws Exception {
        LoginRequest req = new LoginRequest("test@mail.com", "wrong");
        when(userService.login(any())).thenThrow(new UnauthorizedException("E-posta veya şifre hatalı"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
