package com.finanscepte.desktop.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Custom Graphics bileşeni — JavaFX Canvas API ile sıfırdan çizilen
 * Sparkline (mini trend çizgisi) grafiği.
 *
 * Standart JavaFX Chart kütüphaneleri kullanılmamıştır.
 * Tüm çizimler GraphicsContext üzerinde ham 2D komutlarla yapılmaktadır:
 *   moveTo / lineTo / fillPolygon / fillText / strokeLine vb.
 */
public class SparklineCanvas extends Canvas {

    private static final int W = 320;
    private static final int H = 120;
    private static final int PAD = 20;

    private final GraphicsContext gc;
    private List<Double> values;
    private String title;
    private Color lineColor;

    public SparklineCanvas(String title, Color lineColor) {
        super(W, H);
        this.title = title;
        this.lineColor = lineColor;
        gc = getGraphicsContext2D();
        drawEmpty();
    }

    /** Veri listesi gelince grafiği yeniden çiz */
    public void setData(List<Double> values) {
        this.values = values;
        draw();
    }

    private void drawEmpty() {
        gc.clearRect(0, 0, W, H);
        gc.setFill(Color.web("#16213e"));
        gc.fillRoundRect(0, 0, W, H, 12, 12);
        gc.setFont(Font.font("System", 12));
        gc.setFill(Color.web("#555555"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Veri bekleniyor...", W / 2.0, H / 2.0);
    }

    private void draw() {
        if (values == null || values.isEmpty()) { drawEmpty(); return; }

        gc.clearRect(0, 0, W, H);

        // Arkaplan
        gc.setFill(Color.web("#16213e"));
        gc.fillRoundRect(0, 0, W, H, 12, 12);

        // Başlık
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#cccccc"));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(title, PAD, 16);

        double drawW = W - PAD * 2.0;
        double drawH = H - PAD * 2.0 - 10;
        double top = PAD + 10;

        double minV = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxV = values.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double range = maxV - minV;
        if (range == 0) range = 1;

        int n = values.size();

        // ── Yatay grid çizgileri ──────────────────────────────────────────
        gc.setStroke(Color.web("#1e2a45"));
        gc.setLineWidth(1);
        for (int i = 0; i <= 4; i++) {
            double y = top + drawH * i / 4.0;
            gc.strokeLine(PAD, y, W - PAD, y);
        }

        // ── Dolgu alanı (filled area) — polygon olarak çiz ──────────────
        double[] xs = new double[n + 2];
        double[] ys = new double[n + 2];

        for (int i = 0; i < n; i++) {
            xs[i] = PAD + drawW * i / (double) Math.max(n - 1, 1);
            double normalized = (values.get(i) - minV) / range;
            ys[i] = top + drawH * (1 - normalized);
        }
        xs[n]     = xs[n - 1];
        ys[n]     = top + drawH;
        xs[n + 1] = xs[0];
        ys[n + 1] = top + drawH;

        gc.setFill(lineColor.deriveColor(0, 1, 1, 0.15));
        gc.fillPolygon(xs, ys, n + 2);

        // ── Çizgi ────────────────────────────────────────────────────────
        gc.setStroke(lineColor);
        gc.setLineWidth(2.5);
        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < n; i++) {
            // Cubic bezier ile smooth çizgi
            double cpX = (xs[i - 1] + xs[i]) / 2.0;
            gc.bezierCurveTo(cpX, ys[i - 1], cpX, ys[i], xs[i], ys[i]);
        }
        gc.stroke();

        // ── Veri noktaları ────────────────────────────────────────────────
        gc.setFill(lineColor);
        for (int i = 0; i < n; i++) {
            gc.fillOval(xs[i] - 3, ys[i] - 3, 6, 6);
        }

        // ── Son değer etiketi ─────────────────────────────────────────────
        double lastX = xs[n - 1];
        double lastY = ys[n - 1];
        String lastLabel = String.format("%.0f ₺", values.get(n - 1));

        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.setFill(Color.WHITE);
        gc.setFill(lineColor.brighter());
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(lastLabel, Math.min(lastX + 2, W - 4), lastY - 6);

        // ── Min / Max değer etiketleri ────────────────────────────────────
        gc.setFont(Font.font("System", 9));
        gc.setFill(Color.web("#888888"));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(String.format("%.0f ₺", maxV), PAD, top + 9);
        gc.fillText(String.format("%.0f ₺", minV), PAD, top + drawH);
    }
}
