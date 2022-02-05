package de.ipvs.as.mbp.domain.visualization;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a possible visualization a user can choose
 * in the MBP frontend to visualize data.
 */
public class Visualization {

    /**
     * All fields the visualization needs to visualize something. A field
     * must be either primitive or an array of a primitive type.
     * It is conceivable that some visualizations offer multiple field
     * possibilities to visualize something which is why a list of
     * those fields exists.
     */
    private List<VisualizationFields> fieldsToVisualize;

    /**
     * An unique identifier to identify the visualization among others.
     */
    private String name;

    public Visualization(String name) {
        setName(name);
        fieldsToVisualize = new ArrayList<>();
    }

    public Visualization setFieldsToVisualize(List<VisualizationFields> fieldsToVisualize) {
        this.fieldsToVisualize = fieldsToVisualize;
        return this;
    }

    public Visualization addFieldsToVisualize(VisualizationFields fieldToVisualize) {
        this.fieldsToVisualize.add(fieldToVisualize);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<VisualizationFields> getFieldsToVisualize() {
        return fieldsToVisualize;
    }
}
