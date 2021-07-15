package de.ipvs.as.mbp.domain.data_model;

import java.util.List;

/**
 * A DataTreeNode is a representation of one node of the {@link DataModel} tree. This is only
 * the representation needed for receiving tree nodes in a JSON format from the REST API and
 * storing it to the MongoDB.
 */
public class DataTreeNode {

    private String name;
    private String description;

    /**
     * Optional semantic information about the field. Not yet used.
     */
    private String semanticHint;

    /**
     * Type of the node. Must be compatible with all available {@link IoTDataTypes}
     */
    private String type;

    private String unit;
    private String parent;
    private List<String> children;

    /**
     * The size of the array.
     */
    private int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
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

    public String getSemanticHint() {
        return semanticHint;
    }

    public void setSemanticHint(String semanticHint) {
        this.semanticHint = semanticHint;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     *
     * @return true if the node has parents, false if not
     */
    public boolean hasParents() {
        if (this.parent == null || this.parent.trim().equals("")) {
            return false;
        }
        return true;
    }
}
