package com.finanscepte.report.util;

import com.finanscepte.report.dto.ReportRequest;
import com.finanscepte.report.dto.ReportResponse;
import com.finanscepte.report.model.Report;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReportMapper {

    public Report toEntity(ReportRequest request) {
        return Report.builder()
                .userId(request.userId())
                .type(com.finanscepte.report.model.ReportType.valueOf(request.type()))
                .startDate(request.startDate())
                .endDate(request.endDate())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public ReportResponse toResponse(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getUserId(),
                report.getType().name(),
                report.getStartDate(),
                report.getEndDate(),
                report.getData(),
                report.getCreatedAt()
        );
    }
}
