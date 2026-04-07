package models;

import java.util.Random;

public class Graph {
    private final City[] cities;
    private final double[][] distanceMatrix;
    private final int numCities;
    private final int bound;

    public Graph(int numCities, int bound) {
        this.numCities = numCities;
        this.bound = bound;
        this.cities = new City[numCities];
        this.distanceMatrix = new double[numCities][numCities];
        generateRandomCities();
        calculateDistanceMatrix();
    }

    private void generateRandomCities() {
        Random random = new Random();
        for (int i = 0; i < numCities; i++) {
            double x = 50 + random.nextDouble() * (bound - 100);
            double y = 50 + random.nextDouble() * (bound - 100);
            cities[i] = new City(i + 1, x, y);
        }
    }

    private void calculateDistanceMatrix() {
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                distanceMatrix[i][j] = cities[i].distanceTo(cities[j]);
            }
        }
    }

    public double calculateRouteDistance(int[] path) {
        double distance = 0;
        for (int i = 0; i < path.length - 1; i++) {
            distance += distanceMatrix[path[i]][path[i + 1]];
        }
        distance += distanceMatrix[path[path.length - 1]][path[0]];
        return distance;
    }

    public City getCity(int index) { return cities[index]; }
    public City[] getCities() { return cities; }
    public double[][] getDistanceMatrix() { return distanceMatrix; }
    public int getNumCities() { return numCities; }
    public int getBound() { return bound; }

    public void regenerate() {
        generateRandomCities();
        calculateDistanceMatrix();
    }
}