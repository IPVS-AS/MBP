package de.ipvs.as.mbp.domain.visualization;

import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;

import java.util.List;

public class Visualization {

    /**
     * Id of the visualization.
     */
    private String id;

    /**
     * Name of the visualization.
     */
    private String name;

    /**
     * Description of the visualization
     */
    private String description;

    /**
     * Specifies which kind of data can be visualized by this visualization.
     */
    private List<DataModelTreeNode> visualisableDataModels;

    public Visualization(List<DataModelTreeNode> visualisableDataModels) {
        this.visualisableDataModels = visualisableDataModels;
    }

    public String getId() {
        return id;
    }

    public Visualization setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Visualization setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Visualization setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<DataModelTreeNode> getVisualisableDataModels() {
        return visualisableDataModels;
    }

    public Visualization setVisualisableDataModels(List<DataModelTreeNode> visualisableDataModels) {
        this.visualisableDataModels = visualisableDataModels;
        return this;
    }
}
