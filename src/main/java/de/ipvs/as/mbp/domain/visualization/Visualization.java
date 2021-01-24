package de.ipvs.as.mbp.domain.visualization;

import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.data_model.DataTreeNode;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Visualization {

    /**
     * All fields the visualization needs to visualize something. A field
     * must be either primitive or an array of a primitive type.
     * It is conceivable that some visualizations offer multiple field
     * possibilities to visualize something which is why a list of
     * those fields exists.
     */
    private List<VisualizationFields> fieldsToVisualize;

    private String id;

    public Visualization(String id) {
        setId(id);
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<VisualizationFields> getFieldsToVisualize() {
        return fieldsToVisualize;
    }
}
