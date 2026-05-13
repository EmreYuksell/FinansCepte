package com.finanscepte.report.strategy;

import com.finanscepte.report.dto.ReportRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MonthlySummaryStrategy implements ReportGenerationStrategy {

    @Override
    public Map<String, Object> generate(ReportRequest request) {
        return Map.of(
                "type", "MONTHLY_SUMMARY",
                "startDate", request.startDate().toString(),
                "endDate", request.endDate().toString(),
                "summary", "Monthly financial summary generated"
        );
    }

    @Override
    public boolean supports(String reportType) {
        return "MONTHLY_SUMMARY".equalsIgnoreCase(reportType);
    }
}
