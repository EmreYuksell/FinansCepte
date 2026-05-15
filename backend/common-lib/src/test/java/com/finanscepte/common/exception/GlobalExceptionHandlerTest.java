package com.finanscepte.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    void shouldReturn404_forResourceNotFound() {
        ResponseEntity<?> response = handler.handleResourceNotFound(
                new ResourceNotFoundException("Account", "id", "x"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).hasFieldOrPropertyWithValue("status", 404);
    }

    @Test
    void shouldReturn401_forUnauthorized() {
        ResponseEntity<?> response = handler.handleUnauthorized(
                new UnauthorizedException("E-posta veya şifre hatalı"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).hasFieldOrPropertyWithValue("status", 401);
    }

    @Test
    void shouldReturn400_forIllegalArgument() {
        ResponseEntity<?> response = handler.handleIllegalArgument(
                new IllegalArgumentException("Geçersiz veri"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
