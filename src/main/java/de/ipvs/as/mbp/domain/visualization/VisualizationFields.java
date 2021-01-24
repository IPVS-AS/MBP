package de.ipvs.as.mbp.domain.visualization;

import de.ipvs.as.mbp.domain.data_model.DataTreeNode;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;

import java.util.HashMap;
import java.util.Map;

public class VisualizationFields {

    private Map<String, DataModelTreeNode> fieldsToVisualize;

    private String fieldName;

    public VisualizationFields(String fieldName) {
        this.fieldName = fieldName;
        this.fieldsToVisualize = new HashMap<>();
    }

    public String getFieldName() {
        return fieldName;
    }

    public Map<String, DataModelTreeNode> getFieldsToVisualize() {
        return fieldsToVisualize;
    }

    public void setFieldsToVisualize(Map<String, DataModelTreeNode> fieldsToVisualize) {
        this.fieldsToVisualize = fieldsToVisualize;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public VisualizationFields addNewArray(String fieldName, IoTDataTypes primitiveTypeOfArray) {
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

    public VisualizationFields addNewPrimitiveType(String fieldName, IoTDataTypes primitiveType) {
        if (!IoTDataTypes.isPrimitive(primitiveType)) {
            return this;
        }
        this.fieldsToVisualize.put(fieldName, getNewDataModelTreeNode(fieldName, primitiveType));
        return this;
    }


    private static DataModelTreeNode getNewDataModelTreeNode(String fieldName, IoTDataTypes type) {
        DataTreeNode node = new DataTreeNode();
        node.setName(fieldName);
        node.setType(type.getValue());
        DataModelTreeNode modelNode = new DataModelTreeNode(node);
        modelNode.setType(type);
        modelNode.setName(fieldName);
        return modelNode;
    }

}
