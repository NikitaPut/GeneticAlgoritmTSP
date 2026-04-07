package algorithms;

import models.Graph;
import models.Route;
import java.util.*;

public class GeneticAlgorithm {
    private final Graph graph;
    private final Random random;

    // Параметры алгоритма
    private final int populationSize;
    private final int generations;
    private final double mutationRate;
    private final double eliteRate;
    private final int tournamentSize;

    public GeneticAlgorithm(Graph graph) {
        this.graph = graph;
        this.random = new Random();
        this.populationSize = 100;
        this.generations = 500;
        this.mutationRate = 0.02;
        this.eliteRate = 0.1;
        this.tournamentSize = 5;
    }

    public GeneticAlgorithm(Graph graph, int populationSize, int generations,
                            double mutationRate, double eliteRate, int tournamentSize) {
        this.graph = graph;
        this.random = new Random();
        this.populationSize = populationSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.eliteRate = eliteRate;
        this.tournamentSize = tournamentSize;
    }

    public Route solve(OptimizationCallback callback) {
        List<Route> population = initializePopulation();
        Route bestRoute = null;
        double bestDistance = Double.MAX_VALUE;

        for (int generation = 0; generation < generations; generation++) {
            // Оценка популяции
            for (Route route : population) {
                route.setDistance(graph.calculateRouteDistance(route.getPath()));
            }

            // Сортировка
            population.sort(Comparator.naturalOrder());

            // Обновление лучшего решения
            Route currentBest = population.get(0);
            if (currentBest.getDistance() < bestDistance) {
                bestDistance = currentBest.getDistance();
                bestRoute = currentBest.copy();
            }

            // Создание новой популяции
            List<Route> newPopulation = new ArrayList<>();

            // Элитизм
            int eliteCount = (int)(populationSize * eliteRate);
            for (int i = 0; i < eliteCount; i++) {
                newPopulation.add(population.get(i).copy());
            }

            // Создание потомков
            while (newPopulation.size() < populationSize) {
                Route parent1 = tournamentSelection(population);
                Route parent2 = tournamentSelection(population);
                Route child = crossover(parent1, parent2);
                mutate(child);
                newPopulation.add(child);
            }

            population = newPopulation;

            // Коллбэк для визуализации
            if (callback != null && generation % 2 == 0) {
                callback.onIteration(generation, bestRoute, bestDistance);
            }
        }

        return bestRoute;
    }

    private List<Route> initializePopulation() {
        List<Route> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Route route = createRandomRoute();
            route.setDistance(graph.calculateRouteDistance(route.getPath()));
            population.add(route);
        }
        return population;
    }

    private Route createRandomRoute() {
        List<Integer> cities = new ArrayList<>();
        for (int i = 0; i < graph.getNumCities(); i++) {
            cities.add(i);
        }
        Collections.shuffle(cities);
        int[] path = new int[graph.getNumCities()];
        for (int i = 0; i < graph.getNumCities(); i++) {
            path[i] = cities.get(i);
        }
        return new Route(path);
    }

    private Route tournamentSelection(List<Route> population) {
        Route best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Route candidate = population.get(random.nextInt(population.size()));
            if (best == null || candidate.getDistance() < best.getDistance()) {
                best = candidate;
            }
        }
        return best.copy();
    }

    private Route crossover(Route parent1, Route parent2) {
        int n = graph.getNumCities();
        int[] child = new int[n];
        Arrays.fill(child, -1);

        int start = random.nextInt(n);
        int end = random.nextInt(n);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        // Копируем сегмент от первого родителя
        for (int i = start; i <= end; i++) {
            child[i] = parent1.getPath()[i];
        }

        // Заполняем остальные от второго родителя
        for (int i = 0; i < n; i++) {
            if (i >= start && i <= end) continue;

            int value = parent2.getPath()[i];
            if (!contains(child, value)) {
                child[i] = value;
            } else {
                // Разрешение конфликтов
                int pos = findPosition(child, value);
                while (pos >= start && pos <= end) {
                    value = parent2.getPath()[pos];
                    pos = findPosition(child, value);
                }
                child[i] = value;
            }
        }

        // Заполняем оставшиеся позиции
        for (int i = 0; i < n; i++) {
            if (child[i] == -1) {
                child[i] = parent2.getPath()[i];
            }
        }

        return new Route(child);
    }

    private void mutate(Route route) {
        if (random.nextDouble() < mutationRate) {
            int i = random.nextInt(graph.getNumCities());
            int j = random.nextInt(graph.getNumCities());
            route.swapCities(i, j);
        }
    }

    private boolean contains(int[] array, int value) {
        for (int v : array) {
            if (v == value) return true;
        }
        return false;
    }

    private int findPosition(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) return i;
        }
        return -1;
    }

    public interface OptimizationCallback {
        void onIteration(int iteration, Route bestRoute, double bestDistance);
    }
}