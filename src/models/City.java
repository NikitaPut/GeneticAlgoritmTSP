package models;

import java.io.Serializable;

public class City implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final double x;
    private final double y;

    public City(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }

    public double distanceTo(City other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("City{id=%d, x=%.2f, y=%.2f}", id, x, y);
    }
}