package com.finanscepte.goals.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record GoalRequest(
        @NotBlank String userId,
        @NotBlank String name,
        @NotNull @Positive Double targetAmount,
        @NotNull @Positive Double currentAmount,
        @NotNull LocalDate deadline,
        String color,
        String category
) {}
