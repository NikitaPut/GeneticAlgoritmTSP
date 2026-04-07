import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.Timer;

class TSPVisualization extends JPanel {
    // Параметры задачи
    private static final int NUM_CITIES = 20;
    private static final int BOUND = 800;
    private static final int DELAY = 50;

    // Параметры генетического алгоритма
    private static final int POPULATION_SIZE = 100;
    private static final int GENERATIONS = 500;
    private static final double MUTATION_RATE = 0.02;
    private static final double ELITE_RATE = 0.1;
    private static final int TOURNAMENT_SIZE = 5;

    // Параметры имитации отжига
    private static final double INIT_TEMP = 1000.0;
    private static final double COOLING_RATE = 0.995;
    private static final int SA_ITERATIONS = 50000;

    private City[] cities;
    private double[][] distanceMatrix;
    private final Random random = new Random(); // Сделано final

    // Для анимации
    private int[] currentPath;
    private double currentDistance;
    private int currentStep;
    private int totalSteps;
    private String algorithmName;
    private List<Double> history = new ArrayList<>();
    private Timer timer;
    private boolean animationRunning = false;

    static class City {
        double x, y;
        int id;

        City(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        double distanceTo(City other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    static class Route {
        int[] path;
        double distance;

        Route(int[] path) {
            this.path = path.clone();
        }

        Route copy() {
            return new Route(this.path);
        }
    }

    public TSPVisualization() {
        generateCities();
        calculateDistanceMatrix();
        setPreferredSize(new Dimension(1200, 800));
        setBackground(Color.WHITE);
    }

    private void generateCities() {
        cities = new City[NUM_CITIES];
        for (int i = 0; i < NUM_CITIES; i++) {
            double x = 50 + random.nextDouble() * (BOUND - 100);
            double y = 50 + random.nextDouble() * (BOUND - 100);
            cities[i] = new City(i + 1, x, y);
        }
    }

    private void calculateDistanceMatrix() {
        distanceMatrix = new double[NUM_CITIES][NUM_CITIES];
        for (int i = 0; i < NUM_CITIES; i++) {
            for (int j = 0; j < NUM_CITIES; j++) {
                distanceMatrix[i][j] = cities[i].distanceTo(cities[j]);
            }
        }
    }

    private double calculateDistance(int[] path) {
        double dist = 0;
        for (int i = 0; i < path.length - 1; i++) {
            dist += distanceMatrix[path[i]][path[i + 1]];
        }
        dist += distanceMatrix[path[path.length - 1]][path[0]];
        return dist;
    }

    // ==================== ГЕНЕТИЧЕСКИЙ АЛГОРИТМ ====================

    private Route createRandomRoute() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < NUM_CITIES; i++) list.add(i);
        Collections.shuffle(list);
        int[] path = new int[NUM_CITIES];
        for (int i = 0; i < NUM_CITIES; i++) path[i] = list.get(i);
        return new Route(path);
    }

    private List<Route> initializePopulation() {
        List<Route> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Route route = createRandomRoute();
            route.distance = calculateDistance(route.path);
            population.add(route);
        }
        return population;
    }

    private Route tournamentSelection(List<Route> population) {
        Route best = null;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            Route candidate = population.get(random.nextInt(population.size()));
            if (best == null || candidate.distance < best.distance) {
                best = candidate;
            }
        }
        return best.copy();
    }

    private Route crossover(Route parent1, Route parent2) {
        int[] child = new int[NUM_CITIES];
        Arrays.fill(child, -1);

        int start = random.nextInt(NUM_CITIES);
        int end = random.nextInt(NUM_CITIES);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        for (int i = start; i <= end; i++) {
            child[i] = parent1.path[i];
        }

        for (int i = 0; i < NUM_CITIES; i++) {
            if (i >= start && i <= end) continue;
            int value = parent2.path[i];
            if (!contains(child, value)) {
                child[i] = value;
            } else {
                int pos = findPosition(child, value);
                while (pos >= start && pos <= end) {
                    value = parent2.path[pos];
                    pos = findPosition(child, value);
                }
                child[i] = value;
            }
        }

        for (int i = 0; i < NUM_CITIES; i++) {
            if (child[i] == -1) child[i] = parent2.path[i];
        }

        return new Route(child);
    }

    private void mutate(Route route) {
        if (random.nextDouble() < MUTATION_RATE) {
            int i = random.nextInt(NUM_CITIES);
            int j = random.nextInt(NUM_CITIES);
            int temp = route.path[i];
            route.path[i] = route.path[j];
            route.path[j] = temp;
        }
    }

    private boolean contains(int[] array, int value) {
        for (int v : array) if (v == value) return true;
        return false;
    }

    private int findPosition(int[] array, int value) {
        for (int i = 0; i < array.length; i++) if (array[i] == value) return i;
        return -1;
    }

    // ==================== АЛГОРИТМ ИМИТАЦИИ ОТЖИГА ====================

    private Route simulatedAnnealing(Route initialRoute, java.util.function.Consumer<Route> callback) {
        Route current = initialRoute.copy();
        Route best = current.copy();
        double temp = INIT_TEMP;
        int iteration = 0;

        while (temp > 1 && iteration < SA_ITERATIONS) {
            Route neighbor = current.copy();
            int i = random.nextInt(NUM_CITIES);
            int j = random.nextInt(NUM_CITIES);
            int tempVal = neighbor.path[i];
            neighbor.path[i] = neighbor.path[j];
            neighbor.path[j] = tempVal;
            neighbor.distance = calculateDistance(neighbor.path);

            double delta = neighbor.distance - current.distance;

            if (delta < 0 || random.nextDouble() < Math.exp(-delta / temp)) {
                current = neighbor;
                if (current.distance < best.distance) {
                    best = current.copy();
                }
            }

            temp *= COOLING_RATE;
            iteration++;

            if (callback != null && iteration % 50 == 0) {
                callback.accept(current);
            }
        }

        return best;
    }

    // ==================== ВИЗУАЛИЗАЦИЯ ====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2d);

        if (currentPath != null) {
            drawRoute(g2d);
        }

        drawCities(g2d);
        drawInfo(g2d);
        drawConvergenceGraph(g2d);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(230, 230, 230));
        for (int i = 0; i <= BOUND; i += 50) {
            g2d.drawLine(0, i, BOUND, i);
            g2d.drawLine(i, 0, i, BOUND);
        }
    }

    private void drawRoute(Graphics2D g2d) {
        for (int i = 0; i < currentPath.length; i++) {
            int current = currentPath[i];
            int next = currentPath[(i + 1) % currentPath.length];

            City c1 = cities[current];
            City c2 = cities[next];

            float thickness = Math.max(2.0f, 5.0f - (float)(currentDistance / 2000));
            g2d.setStroke(new BasicStroke(thickness));

            float hue = 0.0f + (float)currentStep / Math.max(1, totalSteps) * 0.5f;
            g2d.setColor(Color.getHSBColor(hue, 0.8f, 0.9f));

            g2d.drawLine((int)c1.x, (int)c1.y, (int)c2.x, (int)c2.y);
            drawArrow(g2d, c1, c2);
        }
    }

    private void drawArrow(Graphics2D g2d, City from, City to) {
        double angle = Math.atan2(to.y - from.y, to.x - from.x);
        int arrowX = (int)(to.x - 15 * Math.cos(angle));
        int arrowY = (int)(to.y - 15 * Math.sin(angle));

        int arrowSize = 8;
        int x1 = (int)(arrowX - arrowSize * Math.cos(angle - Math.PI / 6));
        int y1 = (int)(arrowY - arrowSize * Math.sin(angle - Math.PI / 6));
        int x2 = (int)(arrowX - arrowSize * Math.cos(angle + Math.PI / 6));
        int y2 = (int)(arrowY - arrowSize * Math.sin(angle + Math.PI / 6));

        g2d.setColor(new Color(255, 100, 100));
        g2d.drawLine(arrowX, arrowY, x1, y1);
        g2d.drawLine(arrowX, arrowY, x2, y2);
    }

    private void drawCities(Graphics2D g2d) {
        for (City city : cities) {
            RadialGradientPaint grad = new RadialGradientPaint(
                    (float)city.x, (float)city.y, 15,
                    new float[]{0f, 1f},
                    new Color[]{new Color(66, 133, 244), new Color(25, 80, 200)}
            );
            g2d.setPaint(grad);
            g2d.fillOval((int)city.x - 10, (int)city.y - 10, 20, 20);

            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval((int)city.x - 10, (int)city.y - 10, 20, 20);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String text = String.valueOf(city.id);
            int textWidth = fm.stringWidth(text);
            g2d.drawString(text, (int)city.x - textWidth / 2, (int)city.y + 5);
        }
    }

    private void drawInfo(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(10, 10, 280, 120, 15, 15);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Алгоритм: " + algorithmName, 20, 35);
        g2d.drawString("Шаг: " + currentStep + " / " + totalSteps, 20, 60);
        g2d.drawString("Текущее расстояние: " + String.format("%.2f", currentDistance), 20, 85);

        if (history.size() > 0) {
            double best = history.stream().min(Double::compare).orElse(0.0);
            g2d.drawString("Лучшее расстояние: " + String.format("%.2f", best), 20, 110);
        }

        int progressWidth = totalSteps > 0 ? (int)((double)currentStep / totalSteps * 260) : 0;
        g2d.setColor(new Color(66, 133, 244));
        g2d.fillRoundRect(20, 118, progressWidth, 6, 3, 3);
    }

    private void drawConvergenceGraph(Graphics2D g2d) {
        int graphX = BOUND + 20;
        int graphY = 20;
        int graphWidth = 350;
        int graphHeight = 250;

        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRect(graphX, graphY, graphWidth, graphHeight);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Сходимость алгоритма", graphX + 10, graphY + 20);

        if (history.size() > 1 && totalSteps > 0) {
            double min = history.stream().min(Double::compare).orElse(0.0);
            double max = history.stream().max(Double::compare).orElse(1000.0);
            double range = max - min;
            if (range < 0.01) range = 1;

            g2d.setColor(new Color(66, 133, 244, 180));
            g2d.setStroke(new BasicStroke(2));

            int prevX = graphX;
            int prevY = graphY + graphHeight - (int)((history.get(0) - min) / range * (graphHeight - 40));

            for (int i = 1; i < history.size(); i++) {
                int x = graphX + (int)((double)i / totalSteps * graphWidth);
                int y = graphY + graphHeight - (int)((history.get(i) - min) / range * (graphHeight - 40));

                if (x <= graphX + graphWidth) {
                    g2d.drawLine(prevX, prevY, x, y);
                    prevX = x;
                    prevY = y;
                }
            }

            g2d.setColor(Color.RED);
            int currentX = graphX + (int)((double)currentStep / totalSteps * graphWidth);
            int currentY = graphY + graphHeight - (int)((currentDistance - min) / range * (graphHeight - 40));
            g2d.fillOval(currentX - 3, currentY - 3, 6, 6);
        }

        if (history.size() > 0) {
            double best = history.stream().min(Double::compare).orElse(0.0);
            g2d.setColor(new Color(76, 175, 80));
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.drawString("Лучший: " + String.format("%.2f", best), graphX + 10, graphY + graphHeight - 10);
        }
    }

    // ==================== АНИМАЦИЯ ====================

    public void animateGeneticAlgorithm() {
        algorithmName = "ГЕНЕТИЧЕСКИЙ АЛГОРИТМ";
        totalSteps = GENERATIONS;
        history.clear();

        // Создаем final копию для использования в анонимном классе
        final List<Route> initialPopulation = initializePopulation();

        // Сортируем и находим лучший маршрут
        initialPopulation.sort(Comparator.comparingDouble(r -> r.distance));
        currentPath = initialPopulation.get(0).path.clone();
        currentDistance = initialPopulation.get(0).distance;
        history.add(currentDistance);

        // Используем массив для хранения изменяемой популяции
        final List<Route>[] populationHolder = new List[]{initialPopulation};

        timer = new Timer(DELAY, new ActionListener() {
            int generation = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (generation >= GENERATIONS) {
                    timer.stop();
                    animationRunning = false;
                    JOptionPane.showMessageDialog(TSPVisualization.this,
                            "Генетический алгоритм завершен!\nЛучшее расстояние: " + String.format("%.2f", currentDistance),
                            "Завершено", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                List<Route> currentPop = populationHolder[0];
                currentPop.sort(Comparator.comparingDouble(r -> r.distance));

                List<Route> newPopulation = new ArrayList<>();
                int eliteCount = (int)(POPULATION_SIZE * ELITE_RATE);
                for (int i = 0; i < eliteCount; i++) {
                    newPopulation.add(currentPop.get(i).copy());
                }

                while (newPopulation.size() < POPULATION_SIZE) {
                    Route parent1 = tournamentSelection(currentPop);
                    Route parent2 = tournamentSelection(currentPop);
                    Route child = crossover(parent1, parent2);
                    mutate(child);
                    child.distance = calculateDistance(child.path);
                    newPopulation.add(child);
                }

                populationHolder[0] = newPopulation;
                populationHolder[0].sort(Comparator.comparingDouble(r -> r.distance));

                currentPath = populationHolder[0].get(0).path.clone();
                currentDistance = populationHolder[0].get(0).distance;
                history.add(currentDistance);
                currentStep = generation;

                repaint();
                generation++;
            }
        });

        timer.start();
        animationRunning = true;
    }

    public void animateSimulatedAnnealing() {
        algorithmName = "ИМИТАЦИЯ ОТЖИГА";
        totalSteps = SA_ITERATIONS / 50;
        history.clear();

        Route current = createRandomRoute();
        current.distance = calculateDistance(current.path);
        Route best = current.copy();

        currentPath = current.path.clone();
        currentDistance = current.distance;
        history.add(currentDistance);

        // Используем массивы для хранения изменяемых значений
        final Route[] currentHolder = {current};
        final Route[] bestHolder = {best};
        final double[] tempHolder = {INIT_TEMP};
        final int[] iterHolder = {0};
        final int[] stepHolder = {0};

        timer = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (iterHolder[0] >= SA_ITERATIONS || stepHolder[0] >= totalSteps) {
                    timer.stop();
                    animationRunning = false;
                    currentPath = bestHolder[0].path.clone();
                    currentDistance = bestHolder[0].distance;
                    repaint();
                    JOptionPane.showMessageDialog(TSPVisualization.this,
                            "Имитация отжига завершена!\nЛучшее расстояние: " + String.format("%.2f", bestHolder[0].distance),
                            "Завершено", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                for (int k = 0; k < 50 && iterHolder[0] < SA_ITERATIONS; k++) {
                    Route neighbor = currentHolder[0].copy();
                    int i = random.nextInt(NUM_CITIES);
                    int j = random.nextInt(NUM_CITIES);
                    int tempVal = neighbor.path[i];
                    neighbor.path[i] = neighbor.path[j];
                    neighbor.path[j] = tempVal;
                    neighbor.distance = calculateDistance(neighbor.path);

                    double delta = neighbor.distance - currentHolder[0].distance;

                    if (delta < 0 || random.nextDouble() < Math.exp(-delta / tempHolder[0])) {
                        currentHolder[0] = neighbor;
                        if (currentHolder[0].distance < bestHolder[0].distance) {
                            bestHolder[0] = currentHolder[0].copy();
                        }
                    }

                    tempHolder[0] *= COOLING_RATE;
                    iterHolder[0]++;
                }

                currentPath = currentHolder[0].path.clone();
                currentDistance = currentHolder[0].distance;
                history.add(currentDistance);
                currentStep = stepHolder[0];
                stepHolder[0]++;

                repaint();
            }
        });

        timer.start();
        animationRunning = true;
    }

    // ==================== ГЛАВНОЕ ОКНО ====================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Визуализация алгоритмов решения задачи коммивояжера");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            TSPVisualization panel = new TSPVisualization();
            frame.add(panel);

            JPanel controlPanel = new JPanel();
            controlPanel.setBackground(new Color(50, 50, 50));

            JButton gaButton = new JButton("Генетический алгоритм");
            JButton saButton = new JButton("Имитация отжига");
            JButton resetButton = new JButton("Сброс");
            JButton stopButton = new JButton("Стоп");

            Color buttonColor = new Color(66, 133, 244);
            Color hoverColor = new Color(100, 160, 255);

            for (JButton btn : new JButton[]{gaButton, saButton, resetButton, stopButton}) {
                btn.setBackground(buttonColor);
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Arial", Font.BOLD, 12));
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

                btn.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        btn.setBackground(hoverColor);
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        btn.setBackground(buttonColor);
                    }
                });
            }

            gaButton.addActionListener(e -> {
                if (panel.animationRunning && panel.timer != null) {
                    panel.timer.stop();
                }
                panel.animateGeneticAlgorithm();
            });

            saButton.addActionListener(e -> {
                if (panel.animationRunning && panel.timer != null) {
                    panel.timer.stop();
                }
                panel.animateSimulatedAnnealing();
            });

            resetButton.addActionListener(e -> {
                if (panel.animationRunning && panel.timer != null) {
                    panel.timer.stop();
                    panel.animationRunning = false;
                }
                panel.generateCities();
                panel.calculateDistanceMatrix();
                panel.currentPath = null;
                panel.history.clear();
                panel.currentStep = 0;
                panel.totalSteps = 0;
                panel.repaint();
            });

            stopButton.addActionListener(e -> {
                if (panel.timer != null && panel.timer.isRunning()) {
                    panel.timer.stop();
                    panel.animationRunning = false;
                }
            });

            controlPanel.add(gaButton);
            controlPanel.add(saButton);
            controlPanel.add(resetButton);
            controlPanel.add(stopButton);

            frame.add(controlPanel, BorderLayout.SOUTH);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            panel.currentPath = null;
            panel.repaint();
        });
    }
}