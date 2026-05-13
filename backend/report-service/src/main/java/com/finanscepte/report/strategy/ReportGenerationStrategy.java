package com.finanscepte.report.strategy;

import com.finanscepte.report.dto.ReportRequest;
import java.util.Map;

public interface ReportGenerationStrategy {

    Map<String, Object> generate(ReportRequest request);

    boolean supports(String reportType);
}
