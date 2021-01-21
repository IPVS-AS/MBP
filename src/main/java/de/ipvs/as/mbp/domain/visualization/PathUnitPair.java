package de.ipvs.as.mbp.domain.visualization;

import java.util.List;

/**
 * A pair consisting of a string {@link com.jayway.jsonpath.JsonPath} representation
 * and a unit.
 */
public class PathUnitPair {

    private String path;
    private String unit;

    public PathUnitPair(String path, String unit) {
        this.path = path;
        this.unit = unit;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
