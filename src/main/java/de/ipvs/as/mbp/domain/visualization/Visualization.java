package de.ipvs.as.mbp.domain.visualization;

import de.ipvs.as.mbp.domain.data_model.DataTreeNode;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;

import java.util.HashMap;
import java.util.Map;

public class Visualization {

    /**
     * All fields the visualization needs to visualize something. A field
     * must be either primitive or an array of a primitive type.
     */
    private Map<String, DataModelTreeNode> fieldsToVisualize;

    private String id;

    public Visualization(String id) {
        setId(id);
        fieldsToVisualize = new HashMap<>();
    }

    public void setFieldsToVisualize(Map<String, DataModelTreeNode> fieldsToVisualize) {
        this.fieldsToVisualize = fieldsToVisualize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Visualization addNewArray(String fieldName, IoTDataTypes primitiveTypeOfArray) {
        if (!IoTDataTypes.isPrimitive(primitiveTypeOfArray)) {
            // For visualizations only arrays with a primitive type are valid.
            return this;
        }
        DataModelTreeNode arrRoot = getNewDataModelTreeNode(fieldName, IoTDataTypes.ARRAY);
        DataModelTreeNode typeNode = getNewDataModelTreeNode(fieldName, primitiveTypeOfArray);

        arrRoot.addOneChildren(typeNode);
        typeNode.addParent(arrRoot);

        this.fieldsToVisualize.put(fieldName, arrRoot);
        return this;
    }

    public Visualization addNewPrimitiveType(String fieldName, IoTDataTypes primitiveType) {
        if (!IoTDataTypes.isPrimitive(primitiveType)) {
            return this;
        }
        this.fieldsToVisualize.put(fieldName, getNewDataModelTreeNode(fieldName, primitiveType));
        return this;
    }


    private DataModelTreeNode getNewDataModelTreeNode(String fieldName, IoTDataTypes type) {
        DataTreeNode node = new DataTreeNode();
        node.setName(fieldName);
        node.setType(type.getValue());
        DataModelTreeNode modelNode = new DataModelTreeNode(node);
        modelNode.setType(type);
        modelNode.setName(fieldName);
        return modelNode;
    }

    public Map<String, DataModelTreeNode> getFieldsToVisualize() {
        return fieldsToVisualize;
    }
}
