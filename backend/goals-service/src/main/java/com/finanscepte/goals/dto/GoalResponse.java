package com.finanscepte.goals.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalResponse(
        String id,
        String userId,
        String name,
        double targetAmount,
        double currentAmount,
        double progressPercent,
        LocalDate deadline,
        String color,
        String category,
        LocalDateTime createdAt
) {}
