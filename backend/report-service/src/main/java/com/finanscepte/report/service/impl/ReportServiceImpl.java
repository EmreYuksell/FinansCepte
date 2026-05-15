package com.finanscepte.report.service.impl;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.report.dto.ReportRequest;
import com.finanscepte.report.dto.ReportResponse;
import com.finanscepte.report.model.Report;
import com.finanscepte.report.model.ReportType;
import com.finanscepte.report.repository.ReportRepository;
import com.finanscepte.report.service.ReportService;
import com.finanscepte.report.util.ReportMapper;
import com.finanscepte.report.strategy.ReportGenerationStrategy;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.ListItem;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final List<ReportGenerationStrategy> strategies;

    public ReportServiceImpl(ReportRepository reportRepository, ReportMapper reportMapper,
                             List<ReportGenerationStrategy> strategyList) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.strategies = strategyList;
    }

    @Override
    public ReportResponse generate(ReportRequest request) {
        ReportGenerationStrategy strategy = strategies.stream()
                .filter(s -> s.supports(request.type()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No strategy found for report type: " + request.type()));
        Report report = reportMapper.toEntity(request);
        report.setData(strategy.generate(request));
        Report saved = reportRepository.save(report);
        return reportMapper.toResponse(saved);
    }

    @Override
    public ReportResponse findById(String id) {
        return reportRepository.findById(id)
                .map(reportMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
    }

    @Override
    public List<ReportResponse> findAll() {
        return reportRepository.findAll().stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Override
    public List<ReportResponse> findByUserId(String userId) {
        return reportRepository.findByUserId(userId).stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        if (!reportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report", "id", id);
        }
        reportRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> getSummary(String period) {
        // Demo data - gerçek implementasyonda transaction-service'ten çekilir
        Map<String, Object> summary = new HashMap<>();
        summary.put("period", period);
        summary.put("totalIncome", 95000.0);
        summary.put("totalExpense", 67000.0);
        summary.put("netSavings", 28000.0);
        summary.put("savingsRate", 29.5);
        summary.put("avgMonthlyIncome", 15833.0);
        summary.put("avgMonthlyExpense", 11167.0);
        summary.put("transactionCount", 142);
        return summary;
    }

    @Override
    public List<Map<String, Object>> getTrend(String period) {
        List<Map<String, Object>> trend = new ArrayList<>();
        String[] months = {"Ara", "Oca", "Şub", "Mar", "Nis", "May"};
        double[] incomes = {12000, 15000, 14000, 18000, 16000, 20000};
        double[] expenses = {10000, 11000, 9500, 13000, 12000, 11500};
        for (int i = 0; i < months.length; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("month", months[i]);
            m.put("income", incomes[i]);
            m.put("expense", expenses[i]);
            trend.add(m);
        }
        return trend;
    }

    @Override
    public List<Map<String, Object>> getCategoryBreakdown(String period) {
        List<Map<String, Object>> cats = new ArrayList<>();
        String[][] data = {{"Market", "3500"}, {"Kira", "5000"}, {"Ulaşım", "1200"}, {"Eğlence", "2500"}, {"Sağlık", "800"}, {"Diğer", "1500"}};
        for (String[] d : data) {
            Map<String, Object> m = new HashMap<>();
            m.put("category", d[0]);
            m.put("amount", Double.parseDouble(d[1]));
            cats.add(m);
        }
        return cats;
    }

    @Override
    public List<String> getInsights() {
        return Arrays.asList(
            "Tasarruf oranınız %29. Bu gayet iyi!",
            "Mart ayında giderleriniz ortalamanın %16 üzerinde.",
            "Gelirleriniz son 3 ayda %33 arttı.",
            "Yıllık hedefinize ulaşmak için ayda ₺20.800 daha biriktirmelisiniz.",
            "Kira giderleri toplam harcamalarınızın %45'ini oluşturuyor."
        );
    }

    @Override
    public byte[] exportPdf(String period, String userId) {
        Map<String, Object> summary = getSummary(period);
        List<Map<String, Object>> trend = getTrend(period);
        List<Map<String, Object>> cats = getCategoryBreakdown(period);
        List<String> insights = getInsights();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(document, bos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(79, 70, 229));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(30, 41, 59));
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font greenFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(16, 185, 129));
            Font redFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(239, 68, 68));

            // Title
            Paragraph title = new Paragraph("CepteFinans Raporu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(6);
            document.add(title);

            Paragraph sub = new Paragraph("Dönem: " + period + " | Tarih: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20);
            document.add(sub);

            // Summary Table
            Paragraph sumHeader = new Paragraph("Finansal Özet", headerFont);
            sumHeader.setSpacingAfter(10);
            document.add(sumHeader);

            PdfPTable sumTable = new PdfPTable(2);
            sumTable.setWidthPercentage(100);
            sumTable.setWidths(new float[]{3, 2});
            addTableRow(sumTable, "Toplam Gelir", formatTL((Double) summary.get("totalIncome")), headerFont, greenFont);
            addTableRow(sumTable, "Toplam Gider", formatTL((Double) summary.get("totalExpense")), headerFont, redFont);
            addTableRow(sumTable, "Net Tasarruf", formatTL((Double) summary.get("netSavings")), headerFont, greenFont);
            addTableRow(sumTable, "Tasarruf Oranı", "%" + String.format("%.1f", (Double) summary.get("savingsRate")), headerFont, normalFont);
            addTableRow(sumTable, "İşlem Sayısı", String.valueOf(((Number) summary.get("transactionCount")).intValue()), headerFont, normalFont);
            document.add(sumTable);
            document.add(Chunk.NEWLINE);

            // Trend Table
            Paragraph trendHeader = new Paragraph("Aylık Trend", headerFont);
            trendHeader.setSpacingAfter(10);
            document.add(trendHeader);

            PdfPTable trendTable = new PdfPTable(3);
            trendTable.setWidthPercentage(100);
            trendTable.setWidths(new float[]{2, 2, 2});
            addHeaderCell(trendTable, "Ay", headerFont);
            addHeaderCell(trendTable, "Gelir", headerFont);
            addHeaderCell(trendTable, "Gider", headerFont);
            for (Map<String, Object> m : trend) {
                addCell(trendTable, (String) m.get("month"), normalFont);
                addCell(trendTable, formatTL((Double) m.get("income")), greenFont);
                addCell(trendTable, formatTL((Double) m.get("expense")), redFont);
            }
            document.add(trendTable);
            document.add(Chunk.NEWLINE);

            // Category Breakdown
            Paragraph catHeader = new Paragraph("Kategori Dağılımı", headerFont);
            catHeader.setSpacingAfter(10);
            document.add(catHeader);

            PdfPTable catTable = new PdfPTable(2);
            catTable.setWidthPercentage(100);
            catTable.setWidths(new float[]{3, 2});
            addHeaderCell(catTable, "Kategori", headerFont);
            addHeaderCell(catTable, "Tutar", headerFont);
            for (Map<String, Object> c : cats) {
                addCell(catTable, (String) c.get("category"), normalFont);
                addCell(catTable, formatTL((Double) c.get("amount")), normalFont);
            }
            document.add(catTable);
            document.add(Chunk.NEWLINE);

            // Insights
            Paragraph insHeader = new Paragraph("Finansal Öngörüler", headerFont);
            insHeader.setSpacingAfter(10);
            document.add(insHeader);

            com.lowagie.text.List insList = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
            insList.setListSymbol("\u2022 ");
            for (String ins : insights) {
                insList.add(new ListItem(ins, normalFont));
            }
            document.add(insList);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("PDF oluşturulamadı: " + e.getMessage(), e);
        }
        return bos.toByteArray();
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(5);
        table.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(5);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(241, 245, 249));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String formatTL(double amount) {
        return String.format("₺%,.2f", amount);
    }
}
