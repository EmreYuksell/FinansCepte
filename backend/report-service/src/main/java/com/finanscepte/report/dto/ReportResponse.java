package com.finanscepte.report.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ReportResponse(
        String id,
        String userId,
        String type,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Map<String, Object> data,
        LocalDateTime createdAt
) {}
