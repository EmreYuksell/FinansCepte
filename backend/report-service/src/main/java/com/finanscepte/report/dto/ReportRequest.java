package com.finanscepte.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ReportRequest(
        @NotBlank String userId,
        @NotBlank String type,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate
) {}
