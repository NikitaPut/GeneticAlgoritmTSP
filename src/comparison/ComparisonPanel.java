package comparison;

import models.Graph;
import models.Route;
import models.City;
import algorithms.GeneticAlgorithm;
import algorithms.SimulatedAnnealing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Date;

public class ComparisonPanel extends JPanel {
    private Graph sharedGraph;
    private JComboBox<Integer> cityCountCombo;
    private JButton runButton;
    private JButton exportButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private JPanel chartPanel;
    private JProgressBar progressBar;

    private final int[] TEST_CITIES = {20, 30, 50};
    private final int RUNS_PER_CONFIG = 5;

    private Map<String, List<Double>> gaHistory = new HashMap<>();
    private Map<String, List<Double>> saHistory = new HashMap<>();
    private String currentTestCities = "";

    public ComparisonPanel(Graph sharedGraph) {
        this.sharedGraph = sharedGraph;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createControlPanel(), BorderLayout.NORTH);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        createResultsTable();
        centerSplit.setTopComponent(new JScrollPane(resultsTable));
        centerSplit.setBottomComponent(createBottomPanel());
        centerSplit.setResizeWeight(0.5);
        add(centerSplit, BorderLayout.CENTER);

        createProgressPanel();
        add(progressBar, BorderLayout.SOUTH);
    }

    public void updateGraph(Graph newGraph) {
        this.sharedGraph = newGraph;
        logArea.append("\n📊 Граф обновлен. Новые координаты городов загружены.\n");
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Управление тестированием"));

        panel.add(new JLabel("Количество городов:"));
        cityCountCombo = new JComboBox<>();
        for (int cities : TEST_CITIES) {
            cityCountCombo.addItem(cities);
        }
        panel.add(cityCountCombo);

        JButton viewGraphButton = new JButton("🗺 Просмотреть полный граф");
        viewGraphButton.setBackground(new Color(156, 39, 176));
        viewGraphButton.setForeground(Color.WHITE);
        viewGraphButton.setFocusPainted(false);
        viewGraphButton.addActionListener(e -> showFullGraph());
        panel.add(viewGraphButton);

        runButton = new JButton("Запустить тесты");
        runButton.setBackground(new Color(76, 175, 80));
        runButton.setForeground(Color.WHITE);
        runButton.setFocusPainted(false);
        runButton.addActionListener(e -> runTests());
        panel.add(runButton);

        exportButton = new JButton("Экспорт результатов");
        exportButton.setBackground(new Color(33, 150, 243));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.addActionListener(e -> exportResults());
        exportButton.setEnabled(false);
        panel.add(exportButton);

        JButton clearButton = new JButton("Очистить");
        clearButton.addActionListener(e -> clearResults());
        panel.add(clearButton);

        return panel;
    }

    private void createResultsTable() {
        tableModel = new DefaultTableModel(
                new String[]{"Городов", "Алгоритм", "Лучшее расстояние",
                        "Среднее расстояние", "Среднее время (мс)", "Стабильность (%)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("Monospaced", Font.PLAIN, 11));
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultsTable.setRowHeight(22);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawConvergenceChart(g);
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(0, 350));
        chartPanel.setBorder(BorderFactory.createTitledBorder("График сходимости"));
        panel.add(chartPanel, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(0, 150));
        logScroll.setBorder(BorderFactory.createTitledBorder("Лог выполнения"));
        panel.add(logScroll, BorderLayout.SOUTH);

        return panel;
    }

    private void createProgressPanel() {
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
    }

    private void runTests() {
        runButton.setEnabled(false);
        exportButton.setEnabled(false);
        tableModel.setRowCount(0);
        logArea.setText("");
        progressBar.setVisible(true);

        int numCities = (int) cityCountCombo.getSelectedItem();
        currentTestCities = String.valueOf(numCities);
        gaHistory.clear();
        saHistory.clear();

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("\n" + "=".repeat(60) + "\n");
                publish(String.format("=== ЗАПУСК ТЕСТОВ ДЛЯ %d ГОРОДОВ ===\n", numCities));
                publish("=".repeat(60) + "\n\n");

                publish("▶ Запуск Генетического Алгоритма...\n");
                setProgress(25);
                TestResult gaResult = testGeneticAlgorithm(numCities);

                publish("▶ Запуск Имитации Отжига...\n");
                setProgress(75);
                TestResult saResult = testSimulatedAnnealing(numCities);

                addResultToTable(numCities, "Генетический алгоритм", gaResult);
                addResultToTable(numCities, "Имитация отжига", saResult);

                publish("\n" + "=".repeat(60) + "\n");
                publish("✅ ТЕСТИРОВАНИЕ ЗАВЕРШЕНО\n");
                publish(String.format("ГА: Лучшее = %.2f | Среднее = %.2f | Время = %.0f мс\n",
                        gaResult.bestDistance, gaResult.avgDistance, gaResult.avgTime));
                publish(String.format("SA: Лучшее = %.2f | Среднее = %.2f | Время = %.0f мс\n",
                        saResult.bestDistance, saResult.avgDistance, saResult.avgTime));

                setProgress(100);
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    logArea.append(message);
                }
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                runButton.setEnabled(true);
                exportButton.setEnabled(true);
                chartPanel.repaint();
            }
        };

        worker.execute();
    }

    private TestResult testGeneticAlgorithm(int numCities) {
        List<Double> distances = new ArrayList<>();
        List<Long> times = new ArrayList<>();
        List<Double> convergenceHistory = new ArrayList<>();

        for (int run = 0; run < RUNS_PER_CONFIG; run++) {
            publish(String.format("  ГА: запуск %d/%d...\n", run + 1, RUNS_PER_CONFIG));

            GeneticAlgorithm ga = new GeneticAlgorithm(sharedGraph);

            final int currentRun = run;
            long startTime = System.nanoTime();

            Route bestRoute = ga.solve((iteration, bestRoute1, bestDistance) -> {
                if (currentRun == 0 && iteration % 20 == 0) {
                    convergenceHistory.add(bestDistance);
                }
            });

            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1_000_000.0;

            distances.add(bestRoute.getDistance());
            times.add((long) timeMs);

            publish(String.format("    → расстояние = %.2f, время = %.0f мс\n",
                    bestRoute.getDistance(), timeMs));
        }

        if (!convergenceHistory.isEmpty()) {
            gaHistory.put(String.valueOf(numCities), convergenceHistory);
        }

        double bestDist = distances.stream().min(Double::compare).orElse(0.0);
        double avgDist = distances.stream().mapToDouble(d -> d).average().orElse(0.0);
        double avgTime = times.stream().mapToLong(l -> l).average().orElse(0.0);
        double stdDev = calculateStdDev(distances, avgDist);
        double stability = (1 - stdDev / avgDist) * 100;

        return new TestResult(bestDist, avgDist, avgTime, stability);
    }

    private TestResult testSimulatedAnnealing(int numCities) {
        List<Double> distances = new ArrayList<>();
        List<Long> times = new ArrayList<>();
        List<Double> convergenceHistory = new ArrayList<>();

        for (int run = 0; run < RUNS_PER_CONFIG; run++) {
            publish(String.format("  SA: запуск %d/%d...\n", run + 1, RUNS_PER_CONFIG));

            SimulatedAnnealing sa = new SimulatedAnnealing(sharedGraph);

            final int currentRun = run;
            long startTime = System.nanoTime();

            Route bestRoute = sa.solve((iteration, bestRoute1, bestDistance) -> {
                if (currentRun == 0 && iteration % 200 == 0) {
                    convergenceHistory.add(bestDistance);
                }
            });

            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1_000_000.0;

            distances.add(bestRoute.getDistance());
            times.add((long) timeMs);

            publish(String.format("    → расстояние = %.2f, время = %.0f мс\n",
                    bestRoute.getDistance(), timeMs));
        }

        if (!convergenceHistory.isEmpty()) {
            saHistory.put(String.valueOf(numCities), convergenceHistory);
        }

        double bestDist = distances.stream().min(Double::compare).orElse(0.0);
        double avgDist = distances.stream().mapToDouble(d -> d).average().orElse(0.0);
        double avgTime = times.stream().mapToLong(l -> l).average().orElse(0.0);
        double stdDev = calculateStdDev(distances, avgDist);
        double stability = (1 - stdDev / avgDist) * 100;

        return new TestResult(bestDist, avgDist, avgTime, stability);
    }

    private void addResultToTable(int numCities, String algorithm, TestResult result) {
        SwingUtilities.invokeLater(() -> {
            tableModel.addRow(new Object[]{
                    numCities,
                    algorithm,
                    String.format("%.2f", result.bestDistance),
                    String.format("%.2f", result.avgDistance),
                    String.format("%.0f", result.avgTime),
                    String.format("%.1f%%", result.stability)
            });
        });
    }

    private void drawConvergenceChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = chartPanel.getWidth();
        int height = chartPanel.getHeight();
        int padding = 50;
        int graphWidth = width - 2 * padding;
        int graphHeight = height - 2 * padding - 30;

        if (gaHistory.isEmpty() && saHistory.isEmpty()) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            String msg = "Запустите тесты для отображения графика";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            g2d.drawString(msg, width / 2 - msgWidth / 2, height / 2);
            return;
        }

        g2d.setColor(Color.BLACK);
        g2d.drawRect(padding - 2, padding - 2, graphWidth + 4, graphHeight + 4);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2d.drawString("Сравнение сходимости (" + currentTestCities + " городов)",
                padding, padding - 10);

        g2d.drawLine(padding, padding, padding, height - padding - 30);
        g2d.drawLine(padding, height - padding - 30, width - padding, height - padding - 30);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.drawString("Итерация/Поколение", width / 2 - 60, height - 10);

        g2d.rotate(-Math.PI / 2);
        g2d.drawString("Расстояние", -height / 2, 15);
        g2d.rotate(Math.PI / 2);

        List<Double> gaData = gaHistory.get(currentTestCities);
        if (gaData != null && !gaData.isEmpty()) {
            drawLineChart(g2d, gaData, padding, graphWidth, graphHeight,
                    new Color(66, 133, 244), "ГА");
        }

        List<Double> saData = saHistory.get(currentTestCities);
        if (saData != null && !saData.isEmpty()) {
            drawLineChart(g2d, saData, padding, graphWidth, graphHeight,
                    new Color(234, 67, 53), "SA");
        }

        drawLegend(g2d, width, padding);
    }

    private void drawLineChart(Graphics2D g2d, List<Double> data, int padding,
                               int graphWidth, int graphHeight, Color color, String label) {
        if (data.size() < 2) return;

        double min = data.stream().min(Double::compare).orElse(0.0);
        double max = data.stream().max(Double::compare).orElse(1.0);
        double range = max - min;
        if (range < 1) range = 1;

        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2.5f));

        int prevX = padding;
        int prevY = padding + graphHeight - (int)((data.get(0) - min) / range * graphHeight);

        for (int i = 1; i < data.size(); i++) {
            int x = padding + (int)((double)i / (data.size() - 1) * graphWidth);
            int y = padding + graphHeight - (int)((data.get(i) - min) / range * graphHeight);

            if (x <= padding + graphWidth) {
                g2d.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }
    }

    private void drawLegend(Graphics2D g2d, int width, int padding) {
        int legendX = width - 150;
        int legendY = padding + 20;

        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRoundRect(legendX - 5, legendY - 5, 140, 40, 8, 8);

        g2d.setColor(new Color(66, 133, 244));
        g2d.fillRect(legendX, legendY, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Генетический", legendX + 20, legendY + 12);

        g2d.setColor(new Color(234, 67, 53));
        g2d.fillRect(legendX, legendY + 22, 15, 15);
        g2d.drawString("Имитация отжига", legendX + 20, legendY + 34);
    }

    private double calculateStdDev(List<Double> values, double mean) {
        double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }

    /**
     * Показывает окно с полным графом (все возможные связи)
     */
    private void showFullGraph() {
        int numCities = (int) cityCountCombo.getSelectedItem();

        // Создаем копию графа с нужным количеством городов
        Graph displayGraph;
        if (sharedGraph.getNumCities() == numCities) {
            displayGraph = sharedGraph;
        } else {
            // Если текущий граф другого размера, создаем временный
            displayGraph = new Graph(numCities, 800);
        }

        JFrame graphFrame = new JFrame("Полный граф - " + numCities + " городов (все возможные связи)");
        graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        graphFrame.setSize(900, 700);
        graphFrame.setLocationRelativeTo(null);

        GraphPreviewPanel previewPanel = new GraphPreviewPanel(displayGraph, numCities);
        graphFrame.add(previewPanel);
        graphFrame.setVisible(true);
    }

    private void exportResults() {
        JFileChooser fileChooser = new JFileChooser();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileChooser.setSelectedFile(new File("tsp_report_" + timestamp + ".html"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                writer.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
                writer.println("<title>Отчет по тестированию TSP</title>");
                writer.println("<style>");
                writer.println("body{font-family:'Segoe UI',Arial;margin:30px}");
                writer.println("table{border-collapse:collapse;width:100%;margin:20px 0}");
                writer.println("th,td{border:1px solid #ddd;padding:8px;text-align:center}");
                writer.println("th{background:#4CAF50;color:white}");
                writer.println("tr:nth-child(even){background:#f2f2f2}");
                writer.println(".best{background:#e8f5e9;font-weight:bold}");
                writer.println("</style></head><body>");

                writer.println("<h1>Отчет по тестированию TSP</h1>");
                writer.println("<p>Дата: " + new Date() + "</p>");
                writer.println("<p>Запусков: " + RUNS_PER_CONFIG + "</p>");
                writer.println("<h2>Результаты</h2>");
                writer.println("<table>");
                writer.println("<tr><th>Городов</th><th>Алгоритм</th><th>Лучшее</th><th>Среднее</th><th>Время(мс)</th><th>Стабильность</th></tr>");

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    writer.println("<tr>");
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        String value = tableModel.getValueAt(i, j).toString();
                        if (j == 2 && i % 2 == 0) {
                            writer.println("<td class='best'>" + value + "</td>");
                        } else {
                            writer.println("<td>" + value + "</td>");
                        }
                    }
                    writer.println("</tr>");
                }
                writer.println("</table></body></html>");

                logArea.append("✅ Экспорт завершён\n");
                JOptionPane.showMessageDialog(this, "Результаты экспортированы!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
            }
        }
    }

    private void clearResults() {
        tableModel.setRowCount(0);
        logArea.setText("");
        gaHistory.clear();
        saHistory.clear();
        chartPanel.repaint();
    }

    private void publish(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message));
    }

    private void setProgress(int value) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(value));
    }

    private static class TestResult {
        double bestDistance, avgDistance, avgTime, stability;
        TestResult(double best, double avg, double time, double stab) {
            this.bestDistance = best;
            this.avgDistance = avg;
            this.avgTime = time;
            this.stability = stab;
        }
    }
}