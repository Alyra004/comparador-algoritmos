package com.trabalhoav3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChartGenerator {

    private static final Color BG_COLOR = new Color(39, 40, 34);
    private static final Color TEXT_COLOR = new Color(248, 248, 242);
    private static final Color GRID_COLOR = new Color(73, 72, 62);
    
    private static final Paint[] MONOKAI_COLORS = {
            new Color(249, 38, 114),
            new Color(166, 226, 46),
            new Color(102, 217, 239),
            new Color(253, 151, 31)
    };

    private static class CustomBarRenderer extends BarRenderer {
        @Override
        public Paint getItemPaint(int row, int column) {
            return MONOKAI_COLORS[column % MONOKAI_COLORS.length];
        }
    }

    private static void applyDarkTheme(JFreeChart chart) {
        chart.setBackgroundPaint(BG_COLOR);
        chart.getTitle().setPaint(TEXT_COLOR);
        chart.getTitle().setFont(new Font("Consolas", Font.BOLD, 18));
        chart.getCategoryPlot().getDomainAxis().setTickLabelPaint(TEXT_COLOR);
        chart.getCategoryPlot().getDomainAxis().setLabelPaint(TEXT_COLOR);
        chart.getCategoryPlot().getRangeAxis().setTickLabelPaint(TEXT_COLOR);
        chart.getCategoryPlot().getRangeAxis().setLabelPaint(TEXT_COLOR);
    }

    public static JPanel createPerformanceChart(List<BenchmarkResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (BenchmarkResult result : results) {
            dataset.addValue(result.getAverageTimeMs(), "Tempo (ms)", result.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Comparativo de Desempenho", "Arquitetura", "Tempo Médio (ms)",
                dataset, PlotOrientation.VERTICAL, false, true, false
        );

        applyDarkTheme(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setOutlineVisible(false);

        CustomBarRenderer renderer = new CustomBarRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.15);
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setBackground(BG_COLOR);
        return chartPanel;
    }

    public static JPanel createScalabilityChart(List<BenchmarkResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (BenchmarkResult result : results) {
            if (result.getAlgorithmName().contains("CPU")) {
                dataset.addValue(result.getAverageTimeMs(), "Tempo (ms)", result.getAlgorithmName());
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Escalabilidade de Threads (CPU)", "Configuração", "Tempo Médio (ms)",
                dataset, PlotOrientation.VERTICAL, false, true, false
        );

        applyDarkTheme(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setOutlineVisible(false);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(4.0f)); 
        renderer.setSeriesPaint(0, MONOKAI_COLORS[1]);
        renderer.setSeriesShapesVisible(0, true);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setBackground(BG_COLOR);
        return chartPanel;
    }

    public static JPanel createOccurrencesPerBookChart(java.util.Map<String, Integer> ocorrenciasPorLivro) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (java.util.Map.Entry<String, Integer> entry : ocorrenciasPorLivro.entrySet()) {
            dataset.addValue(entry.getValue(), "Ocorrências", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Ocorrências por Livro", "Livro", "Quantidade Encontrada",
                dataset, PlotOrientation.HORIZONTAL, false, true, false
        );

        applyDarkTheme(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setOutlineVisible(false);

        CustomBarRenderer renderer = new CustomBarRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.2);
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setBackground(BG_COLOR);
        return chartPanel;
    }

    public static JPanel createPerformancePerBookChart(java.util.Map<String, java.util.List<BenchmarkResult>> perfPorLivro) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (java.util.Map.Entry<String, java.util.List<BenchmarkResult>> entry : perfPorLivro.entrySet()) {
            String livro = entry.getKey();
            for (BenchmarkResult res : entry.getValue()) {
                dataset.addValue(res.getAverageTimeMs(), res.getAlgorithmName(), livro);
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Desempenho por Livro (ms)", "Livro", "Tempo Médio (ms)",
                dataset, PlotOrientation.HORIZONTAL, true, true, false
        );

        applyDarkTheme(chart);
        chart.getLegend().setBackgroundPaint(BG_COLOR);
        chart.getLegend().setItemPaint(TEXT_COLOR);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.01);
        
        for(int i = 0; i < MONOKAI_COLORS.length; i++){
            renderer.setSeriesPaint(i, MONOKAI_COLORS[i % MONOKAI_COLORS.length]);
        }
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setBackground(BG_COLOR);
        return chartPanel;
    }
}