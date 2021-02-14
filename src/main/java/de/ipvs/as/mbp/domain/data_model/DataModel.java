package de.ipvs.as.mbp.domain.data_model;

import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import de.ipvs.as.mbp.domain.visualization.repo.PathUnitPair;
import de.ipvs.as.mbp.domain.visualization.repo.VisMappingInfo;
import de.ipvs.as.mbp.domain.visualization.repo.VisualizationMappings;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.GeneratedValue;
import java.lang.reflect.Array;
import java.util.ArrayList;
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

    /**
     * Example of how a value json string is to be expected to be sent by an operator
     */
    private String JSONExample;

    /**
     * All possibilities how an instance of this data model can be visualized
     */
    private ArrayList<VisMappingInfo> possibleVisMappings;

    /**
     * All json paths to the leaf nodes of the model
     */
    private List<PathUnitPair> jsonPathsToLeafNodes;

    // ------

    public List<PathUnitPair> getJsonPathsToLeafNodes() {
        return jsonPathsToLeafNodes;
    }

    public void setJsonPathsToLeafNodes(List<PathUnitPair> jsonPathsToLeafNodes) {
        this.jsonPathsToLeafNodes = jsonPathsToLeafNodes;
    }

    public List<VisMappingInfo> getPossibleVisMappings() {
        return possibleVisMappings;
    }

    public void setPossibleVisMappings(ArrayList<VisMappingInfo> possibleVisMappings) {
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
