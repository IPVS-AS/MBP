package de.ipvs.as.mbp.domain.data_model.treelogic;

import com.jayway.jsonpath.JsonPath;
import de.ipvs.as.mbp.domain.data_model.DataTreeNode;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * One node of the {@link DataModelTree}. Some contained fields are optional
 * for some {@link IoTDataTypes}:
 * - Only arrays need a dimension, not used dimensions should be <= 0
 * - Description is always optional
 */
public class DataModelTreeNode {

    /**
     * The data tree node representation as it is stored in the repository
     * {@link de.ipvs.as.mbp.domain.data_model.DataModel DataModel}
     */
    private DataTreeNode repositoryTreeNode;

    private List<DataModelTreeNode> children;

    private DataModelTreeNode parent;

    private String name;

    private IoTDataTypes type;

    private String unit;

    private int size;

    private List<String> treePath;

    private JsonPath pathToNode;

    private List<String> internTreePathHistory;
    private String internPathToNode;


    public List<String> getInternTreePathHistory() {
        return internTreePathHistory;
    }

    public void setInternTreePathHistory(List<String> internTreePath) {
        this.internTreePathHistory = internTreePath;
    }

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
        this.type = IoTDataTypes.getDataTypeWithValue(repoTreeNode.getType().toLowerCase());
        this.unit = repoTreeNode.getUnit();
        this.internTreePathHistory = new ArrayList<>();

        // Add dimension for arrays. If not an array set the dimension value to -1 as default.
        if (this.type != null && this.type == IoTDataTypes.ARRAY) {
            this.size = repoTreeNode.getDimension();
        } else {
            this.size = -1;
        }

        this.treePath = new ArrayList<>();
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

    public IoTDataTypes getType() {
        return type;
    }

    public void setType(IoTDataTypes type) {
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

    public List<String> getTreePath() {
        return treePath;
    }

    /**
     * Execute in preorder after the whole tree is build
     */
    public void updateTreePath() {

        if (this.parent == null) {
            this.pathToNode = JsonPath.compile("$");
            this.internPathToNode = "$";
            this.treePath.add(pathToNode.getPath());
            this.internTreePathHistory.add(internPathToNode);
            return;
        }

        String tmpPath = "";
        String tmpInternPath = "";

        // Get all the path fragments of the parents
        this.treePath.addAll(this.parent.getTreePath());
        this.internTreePathHistory.addAll(this.parent.getInternTreePathHistory());


        if (this.parent.getType() == IoTDataTypes.ARRAY) {
            // If the parent is an array then the current node is not allowed to have a name

        } else {
            // Parent is no array --> names are needed
            tmpPath += "." + this.getName();
            tmpInternPath += "['" + this.getName() + "']";
        }

        if (this.type == IoTDataTypes.ARRAY) {
            // This node is an array --> We need array brackets to signal the array
            tmpPath += "[*]";
            // This is an (mbp) intern representation of JsonPath which stores the dimension of a path too
            tmpInternPath += "[#" + this.size + "#]";
        }

        this.treePath.add(tmpPath);
        this.internTreePathHistory.add(tmpInternPath);

        String finalPath = "";
        String finalInternPath = "";

        for (int i = 0; i < this.treePath.size(); i++) {
            finalPath += this.treePath.get(i);
            finalInternPath += this.internTreePathHistory.get(i);
        }

        this.pathToNode = JsonPath.compile(finalPath);
        this.internPathToNode = finalInternPath;
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
        if (this.getParent() == null) {
            return true;
        }
        return false;
    }

    /**
     * @return true if the node is a leaf, that means has no children - otherwise returns false
     */
    public boolean isLeaf() {
        if (this.getChildren().size() <= 0) {
            return true;
        }
        return false;
    }
}
