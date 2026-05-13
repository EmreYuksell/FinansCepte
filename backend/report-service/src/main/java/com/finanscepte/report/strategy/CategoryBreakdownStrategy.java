package com.finanscepte.report.strategy;

import com.finanscepte.report.dto.ReportRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CategoryBreakdownStrategy implements ReportGenerationStrategy {

    @Override
    public Map<String, Object> generate(ReportRequest request) {
        return Map.of(
                "type", "CATEGORY_BREAKDOWN",
                "startDate", request.startDate().toString(),
                "endDate", request.endDate().toString(),
                "breakdown", "Category breakdown analysis generated"
        );
    }

    @Override
    public boolean supports(String reportType) {
        return "CATEGORY_BREAKDOWN".equalsIgnoreCase(reportType);
    }
}
