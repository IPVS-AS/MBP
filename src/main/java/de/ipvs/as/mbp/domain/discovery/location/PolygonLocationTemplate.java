package de.ipvs.as.mbp.domain.discovery.location;

import java.util.List;

public class PolygonLocationTemplate extends LocationTemplate {
    private List<List<Double>> points;

    public PolygonLocationTemplate() {

    }

    public List<List<Double>> getPoints() {
        return points;
    }

    public PolygonLocationTemplate setPoints(List<List<Double>> points) {
        this.points = points;
        return this;
    }
}
