package algorithms;

import models.Graph;
import models.Route;
import java.util.Random;

public class SimulatedAnnealing {
    private final Graph graph;
    private final Random random;

    // Параметры алгоритма
    private final double initialTemp;
    private final double coolingRate;
    private final int maxIterations;

    public SimulatedAnnealing(Graph graph) {
        this.graph = graph;
        this.random = new Random();
        this.initialTemp = 1000.0;
        this.coolingRate = 0.995;
        this.maxIterations = 50000;
    }

    public SimulatedAnnealing(Graph graph, double initialTemp, double coolingRate, int maxIterations) {
        this.graph = graph;
        this.random = new Random();
        this.initialTemp = initialTemp;
        this.coolingRate = coolingRate;
        this.maxIterations = maxIterations;
    }

    public Route solve(OptimizationCallback callback) {
        Route current = createRandomRoute();
        current.setDistance(graph.calculateRouteDistance(current.getPath()));
        Route best = current.copy();

        double temperature = initialTemp;

        for (int iteration = 0; iteration < maxIterations && temperature > 1; iteration++) {
            // Генерация соседнего решения
            Route neighbor = generateNeighbor(current);
            neighbor.setDistance(graph.calculateRouteDistance(neighbor.getPath()));

            double delta = neighbor.getDistance() - current.getDistance();

            // Принятие решения
            if (delta < 0 || random.nextDouble() < Math.exp(-delta / temperature)) {
                current = neighbor;
                if (current.getDistance() < best.getDistance()) {
                    best = current.copy();
                }
            }

            temperature *= coolingRate;

            // Коллбэк для визуализации
            if (callback != null && iteration % 50 == 0) {
                callback.onIteration(iteration, best, best.getDistance());
            }
        }

        return best;
    }

    private Route createRandomRoute() {
        java.util.List<Integer> cities = new java.util.ArrayList<>();
        for (int i = 0; i < graph.getNumCities(); i++) {
            cities.add(i);
        }
        java.util.Collections.shuffle(cities);
        int[] path = new int[graph.getNumCities()];
        for (int i = 0; i < graph.getNumCities(); i++) {
            path[i] = cities.get(i);
        }
        return new Route(path);
    }

    private Route generateNeighbor(Route route) {
        Route neighbor = route.copy();
        int i = random.nextInt(graph.getNumCities());
        int j = random.nextInt(graph.getNumCities());
        neighbor.swapCities(i, j);
        return neighbor;
    }

    public interface OptimizationCallback {
        void onIteration(int iteration, Route bestRoute, double bestDistance);
    }
}