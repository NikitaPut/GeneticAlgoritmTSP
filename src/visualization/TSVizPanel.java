package visualization;

import models.Graph;
import models.Route;
import models.City;
import algorithms.GeneticAlgorithm;
import algorithms.SimulatedAnnealing;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TSVizPanel extends JPanel {
    private Graph graph;
    private RouteRenderer renderer;
    private ConvergenceChart chart;

    private Route currentRoute;
    private List<Double> history;
    private int currentStep;
    private int totalSteps;
    private String algorithmName;
    private String statusMessage;
    private boolean isRunning;

    private Timer animationTimer;
    private int animationDelay = 50;
    private List<Route> animationFrames;
    private int currentFrame;

    public TSVizPanel(Graph sharedGraph) {
        this.graph = sharedGraph;
        this.renderer = new RouteRenderer();
        this.chart = new ConvergenceChart();
        this.history = new ArrayList<>();
        this.animationFrames = new ArrayList<>();
        this.statusMessage = "Готов к работе";
        this.algorithmName = "Не выбран";
        this.isRunning = false;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(chart, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    public void startGeneticAlgorithm() {
        if (isRunning) return;

        algorithmName = "ГЕНЕТИЧЕСКИЙ АЛГОРИТМ";
        statusMessage = "Запуск генетического алгоритма...";
        history.clear();
        animationFrames.clear();
        isRunning = true;

        GeneticAlgorithm ga = new GeneticAlgorithm(graph);

        new Thread(() -> {
            try {
                Route result = ga.solve((iteration, bestRoute, bestDistance) -> {
                    SwingUtilities.invokeLater(() -> {
                        currentRoute = bestRoute;
                        history.add(bestDistance);
                        currentStep = iteration;
                        totalSteps = 500;
                        statusMessage = String.format("Поколение %d / %d, расстояние: %.2f",
                                iteration, totalSteps, bestDistance);

                        if (iteration % 5 == 0) {
                            animationFrames.add(bestRoute.copy());
                        }

                        repaint();
                        chart.updateData(history, bestDistance, iteration, totalSteps);
                        chart.setTitle(algorithmName);
                    });
                });

                SwingUtilities.invokeLater(() -> {
                    currentRoute = result;
                    statusMessage = "Генетический алгоритм завершен!";
                    isRunning = false;
                    repaint();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusMessage = "Ошибка: " + e.getMessage();
                    isRunning = false;
                });
            }
        }).start();
    }

    public void startSimulatedAnnealing() {
        if (isRunning) return;

        algorithmName = "ИМИТАЦИЯ ОТЖИГА";
        statusMessage = "Запуск имитации отжига...";
        history.clear();
        animationFrames.clear();
        isRunning = true;

        SimulatedAnnealing sa = new SimulatedAnnealing(graph);

        new Thread(() -> {
            try {
                Route result = sa.solve((iteration, bestRoute, bestDistance) -> {
                    SwingUtilities.invokeLater(() -> {
                        currentRoute = bestRoute;
                        history.add(bestDistance);
                        currentStep = iteration / 50;
                        totalSteps = 1000;
                        double temp = Math.pow(0.995, iteration) * 1000;
                        statusMessage = String.format("Итерация %d / 50000, температура: %.2f, расстояние: %.2f",
                                iteration, temp, bestDistance);

                        if (iteration % 200 == 0) {
                            animationFrames.add(bestRoute.copy());
                        }

                        repaint();
                        chart.updateData(history, bestDistance, currentStep, totalSteps);
                        chart.setTitle(algorithmName);
                    });
                });

                SwingUtilities.invokeLater(() -> {
                    currentRoute = result;
                    statusMessage = "Имитация отжига завершена!";
                    isRunning = false;
                    repaint();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusMessage = "Ошибка: " + e.getMessage();
                    isRunning = false;
                });
            }
        }).start();
    }

    public void startAnimation() {
        if (animationFrames.isEmpty()) {
            statusMessage = "Нет кадров для анимации. Запустите алгоритм сначала.";
            return;
        }

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        currentFrame = 0;
        currentRoute = animationFrames.get(0);

        animationTimer = new Timer(animationDelay, e -> {
            if (currentFrame < animationFrames.size() - 1) {
                currentFrame++;
                currentRoute = animationFrames.get(currentFrame);
                repaint();
                statusMessage = String.format("Анимация: кадр %d / %d", currentFrame + 1, animationFrames.size());
            } else {
                animationTimer.stop();
                statusMessage = "Анимация завершена";
            }
        });

        animationTimer.start();
        statusMessage = "Воспроизведение анимации...";
    }

    public void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
            statusMessage = "Анимация остановлена";
        }
    }

    public void setAnimationDelay(int delay) {
        this.animationDelay = Math.max(1, Math.min(100, delay));
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.setDelay(animationDelay);
        }
    }

    public void exportCurrentRoute() {
        if (currentRoute == null) {
            statusMessage = "Нет маршрута для экспорта";
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("route_" + algorithmName.replace(" ", "_") + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(fileChooser.getSelectedFile()))) {
                writer.println("Маршрут, найденный " + algorithmName);
                writer.println("Дата: " + new java.util.Date());
                writer.println("Расстояние: " + currentRoute.getDistance());
                writer.println("\nПорядок обхода городов:");
                int[] path = currentRoute.getPath();
                for (int i = 0; i < path.length; i++) {
                    writer.print((path[i] + 1));
                    if (i < path.length - 1) writer.print(" → ");
                    if ((i + 1) % 15 == 0) writer.println();
                }
                writer.println("\n" + (path[0] + 1));

                statusMessage = "Маршрут экспортирован";
                JOptionPane.showMessageDialog(this, "Маршрут сохранен в файл:\n" + fileChooser.getSelectedFile().getName());
            } catch (Exception e) {
                statusMessage = "Ошибка экспорта: " + e.getMessage();
            }
        }
    }

    public void reset() {
        if (isRunning) {
            if (animationTimer != null) animationTimer.stop();
            isRunning = false;
        }
        currentRoute = null;
        history.clear();
        animationFrames.clear();
        currentStep = 0;
        totalSteps = 0;
        algorithmName = "Не выбран";
        statusMessage = "Граф обновлен. Выберите алгоритм";
        chart.clear();
        repaint();
    }

    public String getStatusMessage() { return statusMessage; }
    public String getAlgorithmName() { return algorithmName; }
    public boolean isRunning() { return isRunning; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int mapWidth = getWidth() - 370;
        int mapHeight = getHeight();

        // Сетка
        g2d.setColor(new Color(230, 230, 230));
        for (int i = 0; i <= graph.getBound(); i += 50) {
            g2d.drawLine(0, i, mapWidth, i);
            g2d.drawLine(i, 0, i, mapHeight);
        }

        // Маршрут
        if (currentRoute != null && currentRoute.getPath() != null) {
            drawRoute(g2d, currentRoute);
        }

        // Города
        drawCities(g2d);

        // Информация
        drawInfoPanel(g2d);
    }

    private void drawRoute(Graphics2D g2d, Route route) {
        int[] path = route.getPath();
        if (path == null || path.length == 0) return;

        City[] cities = graph.getCities();

        for (int i = 0; i < path.length; i++) {
            int current = path[i];
            int next = path[(i + 1) % path.length];

            City c1 = cities[current];
            City c2 = cities[next];

            float hue = totalSteps > 0 ? 0.3f + (float)currentStep / totalSteps * 0.3f : 0.5f;
            g2d.setColor(Color.getHSBColor(hue, 0.8f, 0.9f));
            g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2d.drawLine((int)c1.getX(), (int)c1.getY(), (int)c2.getX(), (int)c2.getY());

            if (currentFrame > animationFrames.size() - 5 || animationFrames.isEmpty()) {
                drawArrow(g2d, c1, c2);
            }
        }
    }

    private void drawArrow(Graphics2D g2d, City from, City to) {
        double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
        int arrowX = (int)(to.getX() - 15 * Math.cos(angle));
        int arrowY = (int)(to.getY() - 15 * Math.sin(angle));

        int x1 = (int)(arrowX - 6 * Math.cos(angle - Math.PI / 6));
        int y1 = (int)(arrowY - 6 * Math.sin(angle - Math.PI / 6));
        int x2 = (int)(arrowX - 6 * Math.cos(angle + Math.PI / 6));
        int y2 = (int)(arrowY - 6 * Math.sin(angle + Math.PI / 6));

        g2d.setColor(new Color(255, 80, 80));
        g2d.drawLine(arrowX, arrowY, x1, y1);
        g2d.drawLine(arrowX, arrowY, x2, y2);
    }

    private void drawCities(Graphics2D g2d) {
        City[] cities = graph.getCities();

        for (City city : cities) {
            int x = (int)city.getX();
            int y = (int)city.getY();

            GradientPaint grad = new GradientPaint(x - 5, y - 5, new Color(66, 133, 244),
                    x + 5, y + 5, new Color(25, 80, 200));
            g2d.setPaint(grad);
            g2d.fillOval(x - 10, y - 10, 20, 20);

            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x - 10, y - 10, 20, 20);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2d.getFontMetrics();
            String text = String.valueOf(city.getId());
            g2d.drawString(text, x - fm.stringWidth(text) / 2, y + 4);
        }
    }

    private void drawInfoPanel(Graphics2D g2d) {
        int panelX = 10;
        int panelY = 10;
        int panelWidth = 280;
        int panelHeight = 150;

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString(algorithmName, panelX + 15, panelY + 25);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2d.drawString(statusMessage, panelX + 15, panelY + 50);

        if (currentRoute != null) {
            g2d.drawString(String.format("Расстояние: %.2f", currentRoute.getDistance()),
                    panelX + 15, panelY + 75);
            g2d.drawString(String.format("Городов: %d", graph.getNumCities()),
                    panelX + 15, panelY + 95);
        }

        if (totalSteps > 0 && isRunning) {
            int progressWidth = (int)((double)currentStep / totalSteps * (panelWidth - 30));
            g2d.setColor(new Color(66, 133, 244));
            g2d.fillRoundRect(panelX + 15, panelY + 115, progressWidth, 6, 3, 3);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(panelX + 15, panelY + 115, panelWidth - 30, 6, 3, 3);
        }

        if (!animationFrames.isEmpty()) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2d.drawString(String.format("Кадров анимации: %d", animationFrames.size()),
                    panelX + 15, panelY + 140);
        }
    }
}