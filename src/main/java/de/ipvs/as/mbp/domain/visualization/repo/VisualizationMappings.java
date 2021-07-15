package de.ipvs.as.mbp.domain.visualization.repo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.ipvs.as.mbp.domain.visualization.*;

/**
 * Maps a {@link de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree} to
 * a visualization field colection by mapping each visualizable field of the visualization
 * field collection to a suitable field of
 * the {@link de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree}.
 *
 * One {@link Visualization} can have 1..* {@link VisualizationMappings} each identified
 * by a {@link VisualizationMappings#fieldCollectionName}.
 */
public class VisualizationMappings {

    /**
     * Key: name of the visualization field, Value: List of json paths which lead to data fields compatible for the field
     */
    private Map<String, List<ValueLogPathObject>> jsonPathPerVisualization;

    /**
     * Name of the field collection. A field collection summarizes multiple data fields a visualization
     * needs to visualize something.
     */
    private String fieldCollectionName;


    public VisualizationMappings(String fieldCollectionName) {
        this.jsonPathPerVisualization = new HashMap<>();
        this.fieldCollectionName = fieldCollectionName;
    }

    public Map<String, List<ValueLogPathObject>> getJsonPathPerVisualizationField() {
        return jsonPathPerVisualization;
    }

    public String getFieldCollectionName() {
        return fieldCollectionName;
    }

    public void setFieldCollectionName(String fieldCollectionName) {
        this.fieldCollectionName = fieldCollectionName;
    }

    public void setJsonPathPerVisualizationField(Map<String, List<ValueLogPathObject>> jsonPathPerVisualizationField) {
        this.jsonPathPerVisualization = jsonPathPerVisualizationField;
    }

    public void addVisualizationField(String id, List<ValueLogPathObject> jsonPathsWithUnits) {
        jsonPathPerVisualization.put(id, jsonPathsWithUnits);
    }


}
