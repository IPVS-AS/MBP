package de.ipvs.as.mbp.domain.data_model;

import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import de.ipvs.as.mbp.domain.visualization.VisualizationMappings;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.GeneratedValue;
import java.util.List;

/**
 * Repository (document) class for storing DataModels. The here stored data models
 * can be converted to a {@link de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree}
 * which provides logic to handle heterogeneous iot data.
 */
@MBPEntity(createValidator = DataModelCreateValidator.class)
public class DataModel extends UserEntity {

    @Id
    @GeneratedValue
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private List<DataTreeNode> treeNodes;

    private String JSONExample;

    private List<VisualizationMappings> possibleVisMappings;

    // ------


    public List<VisualizationMappings> getPossibleVisMappings() {
        return possibleVisMappings;
    }

    public void setPossibleVisMappings(List<VisualizationMappings> possibleVisMappings) {
        this.possibleVisMappings = possibleVisMappings;
    }

    public String getJSONExample() {
        return JSONExample;
    }

    public void setJSONExample(String JSONExample) {
        this.JSONExample = JSONExample;
    }

    public List<DataTreeNode> getTreeNodes() {
        return treeNodes;
    }

    public void setTreeNodes(List<DataTreeNode> treeNodes) {
        this.treeNodes = treeNodes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}