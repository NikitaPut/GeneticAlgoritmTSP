package models;

import java.util.Arrays;

public class Route implements Comparable<Route> {
    private int[] path;
    private double distance;

    public Route(int[] path) {
        this.path = path.clone();
    }

    public int[] getPath() { return path; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public void setPath(int[] path) {
        this.path = path.clone();
    }

    public Route copy() {
        Route copy = new Route(this.path);
        copy.distance = this.distance;
        return copy;
    }

    public void swapCities(int i, int j) {
        int temp = path[i];
        path[i] = path[j];
        path[j] = temp;
    }

    @Override
    public int compareTo(Route other) {
        return Double.compare(this.distance, other.distance);
    }

    @Override
    public String toString() {
        return Arrays.toString(path) + " distance: " + distance;
    }
}