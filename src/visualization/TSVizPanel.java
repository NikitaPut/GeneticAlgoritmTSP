package visualization;

import models.*;
import algorithms.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TSVizPanel extends JPanel {
    private Graph graph;
    private RouteRenderer renderer;
    private ConvergenceChart chart;
    private AnimationController animationController;

    private Route currentRoute;
    private List<Double> history;
    private int currentStep;
    private int totalSteps;
    private String algorithmName;
    private String statusMessage;

    public TSVizPanel() {
        this.graph = new Graph(20, 800);
        this.renderer = new RouteRenderer();
        this.chart = new ConvergenceChart();
        this.animationController = new AnimationController();
        this.history = new ArrayList<>();
        this.statusMessage = "Готов к работе";
        this.algorithmName = "Не выбран";

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Добавляем график справа
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(chart, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    public void startGeneticAlgorithm() {
        stop();

        algorithmName = "ГЕНЕТИЧЕСКИЙ АЛГОРИТМ";
        statusMessage = "Запуск генетического алгоритма...";
        history.clear();

        GeneticAlgorithm ga = new GeneticAlgorithm(graph);

        // Запускаем в отдельном потоке
        new Thread(() -> {
            Route result = ga.solve((iteration, bestRoute, bestDistance) -> {
                SwingUtilities.invokeLater(() -> {
                    currentRoute = bestRoute;
                    history.add(bestDistance);
                    currentStep = iteration;
                    totalSteps = 500;
                    statusMessage = String.format("Поколение %d / %d, расстояние: %.2f",
                            iteration, totalSteps, bestDistance);
                    repaint();
                });
            });

            SwingUtilities.invokeLater(() -> {
                currentRoute = result;
                statusMessage = "Генетический алгоритм завершен!";
                repaint();
            });
        }).start();
    }

    public void startSimulatedAnnealing() {
        stop();

        algorithmName = "ИМИТАЦИЯ ОТЖИГА";
        statusMessage = "Запуск имитации отжига...";
        history.clear();

        SimulatedAnnealing sa = new SimulatedAnnealing(graph);

        new Thread(() -> {
            Route result = sa.solve((iteration, bestRoute, bestDistance) -> {
                SwingUtilities.invokeLater(() -> {
                    currentRoute = bestRoute;
                    history.add(bestDistance);
                    currentStep = iteration / 50;
                    totalSteps = 1000;
                    statusMessage = String.format("Итерация %d / %d, температура: %.2f, расстояние: %.2f",
                            iteration, 50000, Math.pow(0.995, iteration) * 1000, bestDistance);
                    repaint();
                });
            });

            SwingUtilities.invokeLater(() -> {
                currentRoute = result;
                statusMessage = "Имитация отжига завершена!";
                repaint();
            });
        }).start();
    }

    public void reset() {
        stop();
        graph.regenerate();
        currentRoute = null;
        history.clear();
        currentStep = 0;
        totalSteps = 0;
        algorithmName = "Не выбран";
        statusMessage = "Граф сброшен. Выберите алгоритм";
        chart.clear();
        repaint();
    }

    public void stop() {
        animationController.stopAnimation();
    }

    public void setAnimationDelay(int delay) {
        animationController.setDelay(delay);
    }

    public String getStatusMessage() { return statusMessage; }
    public String getAlgorithmName() { return algorithmName; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Рисуем сетку
        renderer.drawGrid(g2d, graph.getBound());

        // Рисуем маршрут
        if (currentRoute != null) {
            double progress = totalSteps > 0 ? (double)currentStep / totalSteps : 0;
            renderer.drawRoute(g2d, graph.getCities(), currentRoute, progress);
        }

        // Рисуем города
        renderer.drawCities(g2d, graph.getCities());

        // Рисуем информационную панель
        drawInfoPanel(g2d);

        // Обновляем график
        chart.updateData(history, currentRoute != null ? currentRoute.getDistance() : 0,
                currentStep, totalSteps);
        chart.setTitle(algorithmName);
    }

    private void drawInfoPanel(Graphics2D g2d) {
        int panelX = 10;
        int panelY = 10;
        int panelWidth = 280;
        int panelHeight = 120;

        // Полупрозрачный фон
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2d.drawString(algorithmName, panelX + 15, panelY + 25);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2d.drawString(statusMessage, panelX + 15, panelY + 50);

        if (currentRoute != null) {
            g2d.drawString(String.format("Расстояние: %.2f", currentRoute.getDistance()),
                    panelX + 15, panelY + 75);
        }

        // Индикатор прогресса
        if (totalSteps > 0) {
            int progressWidth = (int)((double)currentStep / totalSteps * (panelWidth - 30));
            g2d.setColor(new Color(66, 133, 244));
            g2d.fillRoundRect(panelX + 15, panelY + 90, progressWidth, 6, 3, 3);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(panelX + 15, panelY + 90, panelWidth - 30, 6, 3, 3);
        }
    }
}