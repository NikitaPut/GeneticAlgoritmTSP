package visualization;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConvergenceChart extends JPanel {
    private List<Double> history;
    private double currentDistance;
    private int currentStep;
    private int totalSteps;
    private String title;

    public ConvergenceChart() {
        this.history = new ArrayList<>();
        this.title = "Сходимость алгоритма";
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(350, 250));
    }

    public void updateData(List<Double> history, double currentDistance, int currentStep, int totalSteps) {
        this.history = new ArrayList<>(history);
        this.currentDistance = currentDistance;
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
        repaint();
    }

    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    public void clear() {
        history.clear();
        currentStep = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 40;
        int graphWidth = width - 2 * padding;
        int graphHeight = height - 2 * padding;

        // Рамка
        g2d.setColor(Color.GRAY);
        g2d.drawRect(padding - 2, padding - 2, graphWidth + 4, graphHeight + 4);

        // Заголовок
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString(title, padding, padding - 10);

        if (history.isEmpty()) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            g2d.drawString("Нет данных", width / 2 - 40, height / 2);
            return;
        }

        // Находим min и max
        double min = history.stream().min(Double::compare).orElse(0.0);
        double max = history.stream().max(Double::compare).orElse(1000.0);
        double range = max - min;
        if (range < 0.01) range = 1;

        // Рисуем оси
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, padding, padding, height - padding);
        g2d.drawLine(padding, height - padding, width - padding, height - padding);

        // Рисуем график
        g2d.setColor(new Color(66, 133, 244));
        g2d.setStroke(new BasicStroke(2));

        int prevX = padding;
        int prevY = height - padding - (int)((history.get(0) - min) / range * graphHeight);

        for (int i = 1; i < history.size(); i++) {
            int x = padding + (int)((double)i / totalSteps * graphWidth);
            int y = height - padding - (int)((history.get(i) - min) / range * graphHeight);

            if (x <= width - padding) {
                g2d.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }

        // Текущая точка
        g2d.setColor(Color.RED);
        int currentX = padding + (int)((double)currentStep / totalSteps * graphWidth);
        int currentY = height - padding - (int)((currentDistance - min) / range * graphHeight);
        g2d.fillOval(currentX - 4, currentY - 4, 8, 8);

        // Лучший результат
        double best = history.stream().min(Double::compare).orElse(0.0);
        g2d.setColor(new Color(76, 175, 80));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.drawString(String.format("Лучший: %.2f", best), padding, height - padding + 15);

        // Текущий результат
        g2d.setColor(Color.RED);
        g2d.drawString(String.format("Текущий: %.2f", currentDistance), padding, height - padding + 30);
    }
}