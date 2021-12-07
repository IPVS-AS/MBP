package de.ipvs.as.mbp.domain.data_model.treelogic;

import com.jayway.jsonpath.JsonPath;
import de.ipvs.as.mbp.domain.data_model.DataTreeNode;
import de.ipvs.as.mbp.domain.data_model.DataModelDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * One node of the {@link DataModelTree}. Some contained fields are optional
 * for some {@link DataModelDataType}:
 * - Only arrays need a dimension, not used dimensions should be <= 0
 * - Description is always optional
 */
public class DataModelTreeNode {

    /**
     * The data tree node representation as it is stored in the repository
     * {@link de.ipvs.as.mbp.domain.data_model.DataModel DataModel}
     */
    private final DataTreeNode repositoryTreeNode;

    private final List<DataModelTreeNode> children;

    private DataModelTreeNode parent;

    private String name;

    private DataModelDataType type;

    private String unit;

    private int size;

    private List<DataModelTreeNode> predecessors;

    private JsonPath pathToNode;

    private String internPathToNode;

    public String getInternPathToNode() {
        return internPathToNode;
    }

    public void setInternPathToNode(String internPathToNode) {
        this.internPathToNode = internPathToNode;
    }

    /**
     * @param repoTreeNode The tree node representation of the repository
     *                     {@link de.ipvs.as.mbp.domain.data_model.DataModel DataModel}
     */
    public DataModelTreeNode(DataTreeNode repoTreeNode) {
        this.repositoryTreeNode = repoTreeNode;
        this.children = new ArrayList<>();
        this.parent = null;
        this.name = repoTreeNode.getName();
        this.type = DataModelDataType.getDataTypeWithValue(repoTreeNode.getType().toLowerCase());
        this.unit = repoTreeNode.getUnit();
        this.predecessors = new ArrayList<>();

        // Add dimension for arrays. If not an array set the dimension value to -1 as default.
        if (this.type != null && this.type == DataModelDataType.ARRAY) {
            this.size = repoTreeNode.getSize();
        } else {
            this.size = -1;
        }
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataModelDataType getType() {
        return type;
    }

    public void setType(DataModelDataType type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void addOneChildren(DataModelTreeNode childrenToAdd) {
        this.children.add(childrenToAdd);
    }

    public void addMultipleChildren(List<DataModelTreeNode> childrenToAdd) {
        this.children.addAll(childrenToAdd);
    }

    public void addParent(DataModelTreeNode parentToAdd) {
        this.parent = parentToAdd;
    }

    public List<DataModelTreeNode> getPredecessors() {
        return predecessors;
    }

    public void setPredecessors(List<DataModelTreeNode> predecessors) {
        this.predecessors = predecessors;
    }

    /**
     * To execute in preorder after the whole tree is build. Updates
     * the {@link DataModelTreeNode#predecessors} and the json paths
     * to this node.
     */
    public void updateTreePath() {

        // Special case if this node is the root node of a tree. Then the json path is only "$"
        if (this.parent == null) {
            this.pathToNode = JsonPath.compile("$");
            this.internPathToNode = "$";
            return;
        }

        // Update the predecessors list
        this.predecessors.addAll(this.parent.getPredecessors());
        this.predecessors.add(this.parent);

        String tmpPath = "";
        String tmpInternPath = "";

        // If the parent is an array then the current node is not allowed to have a name
        if (this.parent.getType() != DataModelDataType.ARRAY) {
            // Parent is no array --> names are needed
            tmpPath += "." + this.getName();
            tmpInternPath += "['" + this.getName() + "']";
        }

        if (this.type == DataModelDataType.ARRAY) {
            // This node is an array --> We need array brackets to signal the array
            tmpPath += "[*]";
            // This is an (mbp) intern representation of JsonPath which stores the dimension of a path too
            tmpInternPath += "[#" + this.size + "#]";
        }

        String finalPath = this.parent.getJsonPathToNode().getPath() + tmpPath;
        this.pathToNode = JsonPath.compile(finalPath);

        this.internPathToNode = this.parent.getInternPathToNode() + tmpInternPath;
    }

    /**
     * @return The {@link JsonPath} for accessing this nodes subtree from a json.
     */
    public JsonPath getJsonPathToNode() {
        return pathToNode;
    }

    public DataTreeNode getRepositoryTreeNode() {
        return repositoryTreeNode;
    }

    public List<DataModelTreeNode> getChildren() {
        return children;
    }

    public DataModelTreeNode getParent() {
        return parent;
    }

    /**
     * @return true if node has no parents and should be therefore a root, otherwise false
     */
    public boolean isRoot() {
        return this.getParent() == null;
    }

    /**
     * @return true if the node is a leaf, that means has no children - otherwise returns false
     */
    public boolean isLeaf() {
        return this.getChildren().size() <= 0;
    }
}
