package com.finanscepte.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reports")
public class Report {

    @Id
    private String id;

    private String userId;

    private ReportType type;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Map<String, Object> data;

    private LocalDateTime createdAt;
}
