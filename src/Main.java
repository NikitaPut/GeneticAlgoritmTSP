import visualization.TSVizPanel;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame("Визуализация алгоритмов решения задачи коммивояжера");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Создаем панель визуализации
            TSVizPanel vizPanel = new TSVizPanel();
            frame.add(vizPanel, BorderLayout.CENTER);

            // Создаем панель управления
            JPanel controlPanel = createControlPanel(vizPanel);
            frame.add(controlPanel, BorderLayout.SOUTH);

            // Создаем панель статуса
            JPanel statusPanel = createStatusPanel(vizPanel);
            frame.add(statusPanel, BorderLayout.NORTH);

            frame.setPreferredSize(new Dimension(1300, 900));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JPanel createControlPanel(TSVizPanel vizPanel) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(45, 45, 45));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Кнопки управления
        JButton gaButton = createStyledButton("Генетический алгоритм", new Color(66, 133, 244));
        JButton saButton = createStyledButton("Имитация отжига", new Color(234, 67, 53));
        JButton resetButton = createStyledButton("Сброс", new Color(52, 168, 83));
        JButton stopButton = createStyledButton("Стоп", new Color(251, 188, 5));

        gaButton.addActionListener(e -> vizPanel.startGeneticAlgorithm());
        saButton.addActionListener(e -> vizPanel.startSimulatedAnnealing());
        resetButton.addActionListener(e -> vizPanel.reset());
        stopButton.addActionListener(e -> vizPanel.stop());

        // Добавляем слайдер для скорости анимации
        panel.add(createSpeedSlider(vizPanel));

        panel.add(gaButton);
        panel.add(saButton);
        panel.add(resetButton);
        panel.add(stopButton);

        return panel;
    }

    private static JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
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

    private static JPanel createSpeedSlider(TSVizPanel vizPanel) {
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        speedPanel.setBackground(new Color(45, 45, 45));

        JLabel speedLabel = new JLabel("Скорость:");
        speedLabel.setForeground(Color.WHITE);
        speedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JSlider speedSlider = new JSlider(1, 100, 50);
        speedSlider.setBackground(new Color(45, 45, 45));
        speedSlider.setPreferredSize(new Dimension(100, 30));
        speedSlider.addChangeListener(e -> {
            int delay = 101 - speedSlider.getValue();
            vizPanel.setAnimationDelay(delay);
        });

        speedPanel.add(speedLabel);
        speedPanel.add(speedSlider);

        return speedPanel;
    }

    private static JPanel createStatusPanel(TSVizPanel vizPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(60, 60, 60));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel statusLabel = new JLabel("Готов к работе");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel algorithmLabel = new JLabel("Выберите алгоритм");
        algorithmLabel.setForeground(new Color(200, 200, 200));
        algorithmLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(algorithmLabel, BorderLayout.EAST);

        // Обновляем статус при изменении
        Timer timer = new Timer(100, e -> {
            statusLabel.setText(vizPanel.getStatusMessage());
            algorithmLabel.setText(vizPanel.getAlgorithmName());
        });
        timer.start();

        return panel;
    }
}