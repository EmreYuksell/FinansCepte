package com.finanscepte.desktop.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Custom Graphics bileşeni — JavaFX Canvas API ile sıfırdan çizilen
 * animasyonlu finansal hız ölçer (gauge) göstergesi.
 *
 * Standart JavaFX Chart kütüphaneleri kullanılmamıştır.
 * Tüm çizimler GraphicsContext üzerindeki ham 2D API ile yapılmaktadır:
 *   strokeArc / fillArc / strokeLine / fillText vb.
 */
public class FinancialGaugeCanvas extends Canvas {

    private static final int W = 320;
    private static final int H = 200;

    // Animasyon için hedef ve mevcut açı
    private double targetRatio = 0.0;   // 0.0 → 1.0
    private double currentRatio = 0.0;
    private double income = 0;
    private double expense = 0;

    private final GraphicsContext gc;
    private AnimationTimer timer;

    public FinancialGaugeCanvas() {
        super(W, H);
        gc = getGraphicsContext2D();
        drawGauge(0.0);
        startAnimation();
    }

    /** Gelir ve gider değerlerini güncelle — gauge otomatik animate olur */
    public void update(double income, double expense) {
        this.income = income;
        this.expense = expense;
        double total = income + expense;
        this.targetRatio = (total > 0) ? (income / total) : 0.5;
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double diff = targetRatio - currentRatio;
                if (Math.abs(diff) > 0.001) {
                    currentRatio += diff * 0.07; // ease-out
                    drawGauge(currentRatio);
                } else if (Math.abs(diff) > 0) {
                    currentRatio = targetRatio;
                    drawGauge(currentRatio);
                }
            }
        };
        timer.start();
    }

    private void drawGauge(double ratio) {
        gc.clearRect(0, 0, W, H);

        final double cx = W / 2.0;
        final double cy = H - 30;
        final double outerR = 130;
        final double innerR = 85;
        final double trackW = outerR - innerR;

        // ── Arkaplan dairesi (gri track) ──────────────────────────────────
        gc.setStroke(Color.web("#1e2a45"));
        gc.setLineWidth(trackW);
        gc.strokeArc(cx - outerR + trackW / 2, cy - outerR + trackW / 2,
                (outerR - trackW / 2) * 2, (outerR - trackW / 2) * 2,
                0, 180, javafx.scene.shape.ArcType.OPEN);

        // ── Kırmızı (gider) bölge ─────────────────────────────────────────
        gc.setStroke(Color.web("#e94560", 0.85));
        gc.setLineWidth(trackW - 4);
        gc.strokeArc(cx - outerR + trackW / 2, cy - outerR + trackW / 2,
                (outerR - trackW / 2) * 2, (outerR - trackW / 2) * 2,
                0, 180, javafx.scene.shape.ArcType.OPEN);

        // ── Yeşil (gelir) bölge — ratio kadar ────────────────────────────
        double greenDegrees = ratio * 180.0;
        gc.setStroke(Color.web("#4ecca3", 0.9));
        gc.setLineWidth(trackW - 4);
        gc.strokeArc(cx - outerR + trackW / 2, cy - outerR + trackW / 2,
                (outerR - trackW / 2) * 2, (outerR - trackW / 2) * 2,
                0, greenDegrees, javafx.scene.shape.ArcType.OPEN);

        // ── İbre (needle) ─────────────────────────────────────────────────
        double needleAngleRad = Math.toRadians(180 - ratio * 180.0);
        double needleLen = outerR - 10;
        double nx = cx + needleLen * Math.cos(needleAngleRad);
        double ny = cy - needleLen * Math.sin(needleAngleRad);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeLine(cx, cy, nx, ny);

        // İbre merkez noktası
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - 6, cy - 6, 12, 12);
        gc.setFill(Color.web("#0f3460"));
        gc.fillOval(cx - 3, cy - 3, 6, 6);

        // ── Skala çizgileri (tick marks) ──────────────────────────────────
        for (int i = 0; i <= 10; i++) {
            double tickAngle = Math.toRadians(180 - i * 18.0);
            double tickLen = (i % 5 == 0) ? 12 : 6;
            double x1 = cx + (outerR - 2) * Math.cos(tickAngle);
            double y1 = cy - (outerR - 2) * Math.sin(tickAngle);
            double x2 = cx + (outerR - 2 - tickLen) * Math.cos(tickAngle);
            double y2 = cy - (outerR - 2 - tickLen) * Math.sin(tickAngle);
            gc.setStroke(Color.web("#ffffff", 0.5));
            gc.setLineWidth(i % 5 == 0 ? 2 : 1);
            gc.strokeLine(x1, y1, x2, y2);
        }

        // ── Etiketler: %0, %50, %100 ──────────────────────────────────────
        gc.setFont(Font.font("System", FontWeight.BOLD, 10));
        gc.setFill(Color.web("#aaaaaa"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("0%",  cx - outerR - 2, cy + 14);
        gc.fillText("50%", cx,               cy - outerR - 8);
        gc.fillText("100%", cx + outerR + 2, cy + 14);

        // ── Ortada büyük oran yazısı ──────────────────────────────────────
        gc.setFont(Font.font("System", FontWeight.BOLD, 26));
        Color valueColor = ratio >= 0.5 ? Color.web("#4ecca3") : Color.web("#e94560");
        gc.setFill(valueColor);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("%.0f%%", ratio * 100), cx, cy - 18);

        // ── Alt açıklama metni ────────────────────────────────────────────
        gc.setFont(Font.font("System", 11));
        gc.setFill(Color.web("#cccccc"));
        gc.fillText("Gelir / (Gelir + Gider)", cx, cy + 16);

        // ── Başlık ───────────────────────────────────────────────────────
        gc.setFont(Font.font("System", FontWeight.BOLD, 13));
        gc.setFill(Color.web("#e0e0e0"));
        gc.fillText("Finansal Denge Göstergesi", cx, 16);
    }

    public void stop() {
        if (timer != null) timer.stop();
    }
}
