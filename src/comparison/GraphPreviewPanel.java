package comparison;

import models.Graph;
import models.City;
import javax.swing.*;
import java.awt.*;

public class GraphPreviewPanel extends JPanel {
    private Graph graph;
    private int numCities;

    public GraphPreviewPanel(Graph graph, int numCities) {
        this.graph = graph;
        this.numCities = numCities;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int offsetX = 50;
        int offsetY = 50;
        int drawWidth = width - 100;
        int drawHeight = height - 100;

        // Рисуем все связи между городами (полный граф)
        City[] cities = graph.getCities();
        double minX = 0, maxX = 800, minY = 0, maxY = 800;

        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1.0f));

        for (int i = 0; i < numCities; i++) {
            for (int j = i + 1; j < numCities; j++) {
                int x1 = offsetX + (int)(cities[i].getX() / 800 * drawWidth);
                int y1 = offsetY + (int)(cities[i].getY() / 800 * drawHeight);
                int x2 = offsetX + (int)(cities[j].getX() / 800 * drawWidth);
                int y2 = offsetY + (int)(cities[j].getY() / 800 * drawHeight);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Рисуем города
        for (City city : cities) {
            int x = offsetX + (int)(city.getX() / 800 * drawWidth);
            int y = offsetY + (int)(city.getY() / 800 * drawHeight);

            // Градиент
            GradientPaint grad = new GradientPaint(x - 5, y - 5, new Color(66, 133, 244),
                    x + 5, y + 5, new Color(25, 80, 200));
            g2d.setPaint(grad);
            g2d.fillOval(x - 10, y - 10, 20, 20);

            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x - 10, y - 10, 20, 20);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String text = String.valueOf(city.getId());
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(text, x - fm.stringWidth(text) / 2, y + 4);
        }

        // Заголовок
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString("Полный граф (" + numCities + " городов, все возможные связи)", 20, 30);
    }
}