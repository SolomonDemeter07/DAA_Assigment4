package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PlotGenerator {
    private static class Row {
        String algo;
        String input;
        int n;
        int m;
        double timeMs;
        long dpOps;
        long charComps;
    }

    public static void main(String[] args) throws IOException {
        File plotsDir = new File("results/plots");
        if (!plotsDir.exists()) {
            plotsDir.mkdirs();
        }

        List<Row> rows = readCsv("results/results.csv");
        drawRecursiveLogPlot(rows, "results/plots/edit_recursive_log_ops.png");
        drawDpTimePlot(rows, "results/plots/edit_dp_time.png");
        drawSearchRatioPlot(rows, "results/plots/search_comparisons_ratio.png");
        System.out.println("Plots generated successfully in results/plots/");
    }

    private static List<Row> readCsv(String path) throws IOException {
        List<Row> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                Row r = new Row();
                r.algo = p[0];
                r.input = p[1];
                r.n = Integer.parseInt(p[2]);
                r.m = Integer.parseInt(p[3]);
                r.timeMs = Double.parseDouble(p[4]);
                r.dpOps = Long.parseLong(p[5]);
                r.charComps = Long.parseLong(p[6]);
                list.add(r);
            }
        }
        return list;
    }

    private static void drawRecursiveLogPlot(List<Row> rows, String outPath) throws IOException {
        int w = 920, h = 600;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = initGraphics(img, w, h,
                "Edit Distance (Small n): log10(DP Operations) vs Length n",
                "String Length n (characters)",
                "log10(DP Operations)");

        Map<String, List<Row>> series = new LinkedHashMap<>();
        for (Row r : rows) {
            if (r.n <= 12 && r.algo.startsWith("EditDistance")) {
                if (r.algo.equals("EditDistance_TwoRows")) continue;
                String key = r.algo.replace("EditDistance_", "") + " (" + r.input + ")";
                series.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }
        }

        double minX = 3.0, maxX = 12.0;
        double minY = 0.0, maxY = 9.0;
        drawGrid(g, w, h, minX, maxX, minY, maxY, "%.0f", "%.1f");

        Color[] palette = {
                new Color(31, 119, 180), new Color(214, 39, 40),
                new Color(44, 160, 44), new Color(255, 127, 14)
        };

        int idx = 0;
        for (var entry : series.entrySet()) {
            Color c = palette[idx % palette.length];
            List<Point> pts = new ArrayList<>();
            for (Row r : entry.getValue()) {
                double ly = Math.log10(Math.max(1, r.dpOps));
                pts.add(toScreen(r.n, ly, minX, maxX, minY, maxY, w, h));
            }
            drawSeries(g, pts, c, entry.getKey(), idx, w);
            idx++;
        }
        g.dispose();
        ImageIO.write(img, "png", new File(outPath));
    }

    private static void drawDpTimePlot(List<Row> rows, String outPath) throws IOException {
        int w = 920, h = 600;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = initGraphics(img, w, h,
                "Edit Distance Table vs TwoRows: Median Time (ms) vs Length n",
                "String Length n (characters)",
                "Median Time (ms)");

        Map<String, List<Row>> series = new LinkedHashMap<>();
        double maxMeasured = 10.0;
        for (Row r : rows) {
            if (r.n >= 500 && r.algo.startsWith("EditDistance")) {
                String key = r.algo.replace("EditDistance_", "") + " (" + r.input + ")";
                series.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
                if (r.timeMs > maxMeasured) maxMeasured = r.timeMs;
            }
        }

        double minX = 500.0, maxX = 4000.0;
        double minY = 0.0, maxY = Math.ceil(maxMeasured * 1.15);
        drawGrid(g, w, h, minX, maxX, minY, maxY, "%.0f", "%.1f");

        Color[] palette = {
                new Color(31, 119, 180), new Color(255, 127, 14),
                new Color(44, 160, 44), new Color(214, 39, 40)
        };

        int idx = 0;
        for (var entry : series.entrySet()) {
            Color c = palette[idx % palette.length];
            List<Point> pts = new ArrayList<>();
            for (Row r : entry.getValue()) {
                pts.add(toScreen(r.n, r.timeMs, minX, maxX, minY, maxY, w, h));
            }
            drawSeries(g, pts, c, entry.getKey(), idx, w);
            idx++;
        }
        g.dispose();
        ImageIO.write(img, "png", new File(outPath));
    }

    private static void drawSearchRatioPlot(List<Row> rows, String outPath) throws IOException {
        int w = 920, h = 600;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = initGraphics(img, w, h,
                "String Search Efficiency: log10(Comparisons / n) vs Text Length n",
                "Text Length n (characters)",
                "log10(Comparisons per Text Char)");

        Map<String, List<Row>> series = new LinkedHashMap<>();
        for (Row r : rows) {
            if (r.algo.startsWith("Search_")) {
                String key = r.algo.replace("Search_", "") + " (" + r.input + ")";
                series.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }
        }

        double minX = 25000.0, maxX = 200000.0;
        double minY = -0.2, maxY = 2.8;
        drawGrid(g, w, h, minX, maxX, minY, maxY, "%.0f", "%.2f");

        Color[] palette = {
                new Color(31, 119, 180), new Color(44, 160, 44),
                new Color(214, 39, 40), new Color(148, 103, 189)
        };

        int idx = 0;
        for (var entry : series.entrySet()) {
            Color c = palette[idx % palette.length];
            List<Point> pts = new ArrayList<>();
            for (Row r : entry.getValue()) {
                double ratio = (double) r.charComps / r.n;
                double ly = Math.log10(Math.max(0.1, ratio));
                pts.add(toScreen(r.n, ly, minX, maxX, minY, maxY, w, h));
            }
            drawSeries(g, pts, c, entry.getKey(), idx, w);
            idx++;
        }
        g.dispose();
        ImageIO.write(img, "png", new File(outPath));
    }

    private static Graphics2D initGraphics(BufferedImage img, int w, int h, String title, String xLabel, String yLabel) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        g.drawString(title, 70, 35);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString(xLabel, w / 2 - 90, h - 18);
        g.drawString(yLabel, 20, 55);
        return g;
    }

    private static void drawGrid(Graphics2D g, int w, int h, double minX, double maxX, double minY, double maxY, String xFmt, String yFmt) {
        int left = 80, right = w - 250, top = 70, bottom = h - 60;
        g.setColor(new Color(230, 230, 230));
        for (int i = 0; i <= 5; i++) {
            int y = bottom - i * (bottom - top) / 5;
            g.drawLine(left, y, right, y);
            double val = minY + i * (maxY - minY) / 5.0;
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.format(Locale.US, yFmt, val), left - 50, y + 5);
            g.setColor(new Color(230, 230, 230));
        }
        for (int i = 0; i <= 4; i++) {
            int x = left + i * (right - left) / 4;
            g.drawLine(x, top, x, bottom);
            double val = minX + i * (maxX - minX) / 4.0;
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.format(Locale.US, xFmt, val), x - 20, bottom + 22);
            g.setColor(new Color(230, 230, 230));
        }
        g.setColor(Color.BLACK);
        g.drawRect(left, top, right - left, bottom - top);
    }

    private static Point toScreen(double x, double y, double minX, double maxX, double minY, double maxY, int w, int h) {
        int left = 80, right = w - 250, top = 70, bottom = h - 60;
        int sx = (int) (left + (x - minX) / (maxX - minX) * (right - left));
        int sy = (int) (bottom - (y - minY) / (maxY - minY) * (bottom - top));
        return new Point(sx, sy);
    }

    private static void drawSeries(Graphics2D g, List<Point> pts, Color c, String label, int idx, int w) {
        g.setColor(c);
        g.setStroke(new BasicStroke(2.4f));
        for (int i = 0; i < pts.size() - 1; i++) {
            g.drawLine(pts.get(i).x, pts.get(i).y, pts.get(i + 1).x, pts.get(i + 1).y);
        }
        for (Point p : pts) {
            g.fillOval(p.x - 4, p.y - 4, 8, 8);
        }
        int lx = w - 235;
        int ly = 90 + idx * 30;
        g.fillRect(lx, ly - 8, 18, 10);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString(label, lx + 24, ly + 1);
    }
}