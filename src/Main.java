import javax.swing.*;
import java.awt.*;
import comparison.ComparisonPanel;
import visualization.TSVizPanel;
import models.Graph;

public class Main extends JFrame {
    private JTabbedPane tabbedPane;
    private TSVizPanel visualizationPanel;
    private ComparisonPanel comparisonPanel;

    // Общий граф для всех вкладок
    private Graph sharedGraph;
    private static final int NUM_CITIES = 20;
    private static final int BOUND = 800;

    public Main() {
        setTitle("TSP: Сравнение алгоритмов оптимизации (ГА vs Имитация отжига)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        // Создаем ОДИН общий граф
        sharedGraph = new Graph(NUM_CITIES, BOUND);

        tabbedPane = new JTabbedPane();

        // Передаем общий граф в обе панели
        visualizationPanel = new TSVizPanel(sharedGraph);
        tabbedPane.addTab("Визуализация", createVisualizationPanel());

        comparisonPanel = new ComparisonPanel(sharedGraph);
        tabbedPane.addTab("Сравнение результатов", comparisonPanel);

        add(tabbedPane);
    }

    /**
     * Создает панель визуализации с элементами управления
     */
    private JPanel createVisualizationPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(visualizationPanel, BorderLayout.CENTER);
        mainPanel.add(createAnimationControlPanel(), BorderLayout.SOUTH);
        return mainPanel;
    }

    /**
     * Создает панель управления анимацией и алгоритмами
     */
    private JPanel createAnimationControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 45));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Панель кнопок алгоритмов
        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        algorithmPanel.setBackground(new Color(45, 45, 45));

        JButton gaButton = createStyledButton("🧬 Генетический алгоритм", new Color(66, 133, 244));
        JButton saButton = createStyledButton("🔥 Имитация отжига", new Color(234, 67, 53));
        JButton resetButton = createStyledButton("🔄 Сброс (новый граф)", new Color(158, 158, 158));

        gaButton.addActionListener(e -> visualizationPanel.startGeneticAlgorithm());
        saButton.addActionListener(e -> visualizationPanel.startSimulatedAnnealing());
        resetButton.addActionListener(e -> {
            sharedGraph.regenerate();
            visualizationPanel.reset();
            comparisonPanel.updateGraph(sharedGraph);
            JOptionPane.showMessageDialog(this, "Граф обновлен! Теперь тесты будут на новых данных.",
                    "Граф сброшен", JOptionPane.INFORMATION_MESSAGE);
        });

        algorithmPanel.add(gaButton);
        algorithmPanel.add(saButton);
        algorithmPanel.add(resetButton);

        // Панель управления анимацией
        JPanel animationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        animationPanel.setBackground(new Color(45, 45, 45));

        JButton playAnimationButton = createStyledButton("▶ Воспроизвести анимацию", new Color(76, 175, 80));
        JButton stopAnimationButton = createStyledButton("⏹ Остановить", new Color(244, 67, 54));
        JButton exportRouteButton = createStyledButton("💾 Экспорт маршрута", new Color(255, 152, 0));

        playAnimationButton.addActionListener(e -> visualizationPanel.startAnimation());
        stopAnimationButton.addActionListener(e -> visualizationPanel.stopAnimation());
        exportRouteButton.addActionListener(e -> visualizationPanel.exportCurrentRoute());

        animationPanel.add(playAnimationButton);
        animationPanel.add(stopAnimationButton);
        animationPanel.add(exportRouteButton);

        // Панель скорости анимации
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        speedPanel.setBackground(new Color(45, 45, 45));

        JLabel speedLabel = new JLabel("⚡ Скорость: средняя");
        speedLabel.setForeground(Color.WHITE);
        speedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JSlider speedSlider = new JSlider(1, 100, 50);
        speedSlider.setBackground(new Color(45, 45, 45));
        speedSlider.setPreferredSize(new Dimension(120, 30));
        speedSlider.addChangeListener(e -> {
            int delay = 101 - speedSlider.getValue();
            visualizationPanel.setAnimationDelay(delay);

            if (delay < 20) speedLabel.setText("⚡ Скорость: очень быстрая");
            else if (delay < 40) speedLabel.setText("⚡ Скорость: быстрая");
            else if (delay < 60) speedLabel.setText("⚡ Скорость: средняя");
            else if (delay < 80) speedLabel.setText("⚡ Скорость: медленная");
            else speedLabel.setText("⚡ Скорость: очень медленная");
        });

        speedPanel.add(speedLabel);
        speedPanel.add(speedSlider);

        // Собираем все в одну строку
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(new Color(45, 45, 45));
        topRow.add(algorithmPanel, BorderLayout.WEST);
        topRow.add(animationPanel, BorderLayout.CENTER);
        topRow.add(speedPanel, BorderLayout.EAST);

        panel.add(topRow, BorderLayout.NORTH);

        // Панель статуса
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statusPanel.setBackground(new Color(60, 60, 60));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel statusIcon = new JLabel("●");
        statusIcon.setForeground(new Color(76, 175, 80));
        statusIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel statusText = new JLabel("Готов к работе");
        statusText.setForeground(Color.WHITE);
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Обновление статуса
        Timer statusTimer = new Timer(200, e -> {
            statusText.setText(visualizationPanel.getStatusMessage());
            if (visualizationPanel.isRunning()) {
                statusIcon.setForeground(new Color(255, 152, 0));
            } else {
                statusIcon.setForeground(new Color(76, 175, 80));
            }
        });
        statusTimer.start();

        statusPanel.add(statusIcon);
        statusPanel.add(statusText);

        panel.add(statusPanel, BorderLayout.CENTER);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Main().setVisible(true);
        });
    }
}