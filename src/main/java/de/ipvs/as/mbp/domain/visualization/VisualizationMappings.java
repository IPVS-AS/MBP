package de.ipvs.as.mbp.domain.visualization;

import java.nio.file.Path;
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

    public VisualizationMappings() {
        this.jsonPathPerVisualization = new HashMap<>();
    }

    public Map<String, List<PathUnitPair>> getJsonPathPerVisualizationField() {
        return jsonPathPerVisualization;
    }

    public void setJsonPathPerVisualizationField(Map<String, List<PathUnitPair>> jsonPathPerVisualizationField) {
        this.jsonPathPerVisualization = jsonPathPerVisualizationField;
    }

    public void addVisualizationField(String id, List<PathUnitPair> jsonPathsWithUnits) {
        jsonPathPerVisualization.put(id, jsonPathsWithUnits);
    }


}
