package de.ipvs.as.mbp.domain.visualization.repo;

import java.util.Objects;

/**
 * Groups data model node information for visualization purposes. Stores the json path
 * to the node as well as properties like its name, unit, type and dimension.
 */
public class PathUnitPair {

    private String path;
    private String name;
    private String unit;
    private String type;
    private int dimension;

    public String getName() {
        return name;
    }

    public PathUnitPair setName(String name) {
        this.name = name;
        return this;
    }

    public int getDimension() {
        return dimension;
    }

    public PathUnitPair setDimension(int dimension) {
        this.dimension = dimension;
        return this;
    }

    public String getType() {
        return type;
    }

    public PathUnitPair setType(String type) {
        this.type = type;
        return this;
    }

    public String getPath() {
        return path;
    }

    public PathUnitPair setPath(String path) {
        this.path = path;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public PathUnitPair setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathUnitPair that = (PathUnitPair) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
