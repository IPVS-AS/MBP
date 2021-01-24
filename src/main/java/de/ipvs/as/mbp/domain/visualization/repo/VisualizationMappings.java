package de.ipvs.as.mbp.domain.visualization.repo;

import de.ipvs.as.mbp.domain.visualization.repo.PathUnitPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps a {@link de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree} to
 * a visualization by mapping each visualizable field of the visualization
 * to a suitable field of the {@link de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree}.
 */
public class VisualizationMappings {

    /**
     * Key: VisualizationFieldName, Value: List of jsonPaths-unit mappings to access nodes of the data model tree.
     */
    private Map<String, List<PathUnitPair>> jsonPathPerVisualization;

    private String fieldCollectionName;


    public VisualizationMappings(String fieldCollectionName) {
        this.jsonPathPerVisualization = new HashMap<>();
        this.fieldCollectionName = fieldCollectionName;
    }

    public Map<String, List<PathUnitPair>> getJsonPathPerVisualizationField() {
        return jsonPathPerVisualization;
    }

    public String getFieldCollectionName() {
        return fieldCollectionName;
    }

    public void setFieldCollectionName(String fieldCollectionName) {
        this.fieldCollectionName = fieldCollectionName;
    }

    public void setJsonPathPerVisualizationField(Map<String, List<PathUnitPair>> jsonPathPerVisualizationField) {
        this.jsonPathPerVisualization = jsonPathPerVisualizationField;
    }

    public void addVisualizationField(String id, List<PathUnitPair> jsonPathsWithUnits) {
        jsonPathPerVisualization.put(id, jsonPathsWithUnits);
    }


}
