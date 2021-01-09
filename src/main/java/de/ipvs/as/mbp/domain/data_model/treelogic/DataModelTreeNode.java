package de.ipvs.as.mbp.domain.data_model.treelogic;

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

    private int dimension;

    private List<String> treePath;

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

        // Add dimension for arrays. If not an array set the dimension value to -1 as default.
        if (this.type != null && this.type == IoTDataTypes.ARRAY) {
            this.dimension = repoTreeNode.getDimension();
        } else {
            this.dimension = -1;
        }

        this.treePath = new ArrayList<>();
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

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
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
        if (this.parent != null) {
            this.treePath.addAll(this.parent.getTreePath());
        }
        this.treePath.add(this.name);
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
