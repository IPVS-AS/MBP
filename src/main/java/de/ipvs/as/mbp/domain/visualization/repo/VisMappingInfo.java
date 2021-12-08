package de.ipvs.as.mbp.domain.visualization.repo;

import java.util.ArrayList;
import java.util.List;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree;
import de.ipvs.as.mbp.domain.visualization.Visualization;

/**
 * Stores all {@link DataModelTree}-{@link Visualization} mappings information
 * which are needed to let the front-end know how data from a sensor/actuator with
 * a certain data model can be visualized.
 */
public class VisMappingInfo {

    private List<VisualizationMappings> mappingPerVisualizationField;

    private String visName;

    public String getVisName() {
        return visName;
    }

    public void setVisName(String visName) {
        this.visName = visName;
    }

    public List<VisualizationMappings> getMappingPerVisualizationField() {
        return mappingPerVisualizationField;
    }

    public void setMappingPerVisualizationField(List<VisualizationMappings> mappingPerVisualizationField) {
        this.mappingPerVisualizationField = mappingPerVisualizationField;
    }

    public void addVisMapping(VisualizationMappings mappings) {
        if (mappingPerVisualizationField == null) {
            this.mappingPerVisualizationField = new ArrayList<>();
        }
        this.mappingPerVisualizationField.add(mappings);
    }

}
