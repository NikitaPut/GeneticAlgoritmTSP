package visualization;

import models.City;
import models.Route;
import java.awt.*;
import java.awt.geom.Line2D;

public class RouteRenderer {
    private static final Color CITY_FILL = new Color(66, 133, 244);
    private static final Color CITY_BORDER = Color.WHITE;
    private static final Color ROUTE_START = new Color(255, 100, 100);
    private static final Color ROUTE_END = new Color(76, 175, 80);

    public void drawCities(Graphics2D g2d, City[] cities) {
        for (City city : cities) {
            drawCity(g2d, city);
        }
    }

    private void drawCity(Graphics2D g2d, City city) {
        int x = (int)city.getX();
        int y = (int)city.getY();

        // Градиентная заливка
        GradientPaint grad = new GradientPaint(
                x - 5, y - 5, CITY_FILL.brighter(),
                x + 5, y + 5, CITY_FILL.darker()
        );
        g2d.setPaint(grad);
        g2d.fillOval(x - 10, y - 10, 20, 20);

        // Обводка
        g2d.setColor(CITY_BORDER);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x - 10, y - 10, 20, 20);

        // Номер города
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(city.getId());
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, x - textWidth / 2, y + 4);
    }

    public void drawRoute(Graphics2D g2d, City[] cities, Route route, double progress) {
        if (route == null) return;

        int[] path = route.getPath();

        for (int i = 0; i < path.length; i++) {
            City from = cities[path[i]];
            City to = cities[path[(i + 1) % path.length]];

            // Интерполяция цвета в зависимости от прогресса
            float hue = (float)(progress * 0.5);
            Color routeColor = Color.getHSBColor(hue, 0.8f, 0.9f);

            g2d.setColor(routeColor);
            g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(new Line2D.Double(from.getX(), from.getY(), to.getX(), to.getY()));

            // Рисуем стрелку направления
            drawArrow(g2d, from, to);
        }
    }

    private void drawArrow(Graphics2D g2d, City from, City to) {
        double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
        int arrowX = (int)(to.getX() - 15 * Math.cos(angle));
        int arrowY = (int)(to.getY() - 15 * Math.sin(angle));

        int arrowSize = 6;
        int x1 = (int)(arrowX - arrowSize * Math.cos(angle - Math.PI / 6));
        int y1 = (int)(arrowY - arrowSize * Math.sin(angle - Math.PI / 6));
        int x2 = (int)(arrowX - arrowSize * Math.cos(angle + Math.PI / 6));
        int y2 = (int)(arrowY - arrowSize * Math.sin(angle + Math.PI / 6));

        g2d.setColor(new Color(255, 100, 100));
        g2d.drawLine(arrowX, arrowY, x1, y1);
        g2d.drawLine(arrowX, arrowY, x2, y2);
    }

    public void drawGrid(Graphics2D g2d, int bound) {
        g2d.setColor(new Color(230, 230, 230));
        g2d.setStroke(new BasicStroke(1));

        for (int i = 0; i <= bound; i += 50) {
            g2d.drawLine(0, i, bound, i);
            g2d.drawLine(i, 0, i, bound);
        }
    }
}