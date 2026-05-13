package com.finanscepte.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @NotBlank String userId,
        @NotBlank String type,
        @NotBlank String message
) {}
