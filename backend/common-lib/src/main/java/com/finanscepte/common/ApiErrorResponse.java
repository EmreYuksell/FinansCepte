package com.finanscepte.common;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}
