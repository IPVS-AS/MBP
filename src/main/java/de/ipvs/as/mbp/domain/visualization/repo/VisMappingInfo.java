package de.ipvs.as.mbp.domain.visualization.repo;

import java.util.ArrayList;
import java.util.List;

/**
 *
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
