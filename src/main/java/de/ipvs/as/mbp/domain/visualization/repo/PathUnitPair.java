package de.ipvs.as.mbp.domain.visualization.repo;

import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;

import java.util.List;

/**
 * A pair consisting of a string {@link com.jayway.jsonpath.JsonPath} representation
 * and a unit.
 */
public class PathUnitPair {

    private String path;
    private String unit;
    private String type;

    public PathUnitPair(String path, String unit, String type) {
        this.path = path;
        this.unit = unit;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
