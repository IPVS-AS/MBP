package de.ipvs.as.mbp.domain.visualization;

import de.ipvs.as.mbp.domain.data_model.DataTreeNode;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;

import java.util.*;

/**
 * Represents a data type list of a visualization. One {@link Visualization} can
 * have 1..n VisualizationFields.
 */
public class VisualizationFields {

    private Map<String, List<DataModelTreeNode>> fieldsToVisualize;

    /**
     * A data type field needs to have a name to be able to distinguish between those
     * alternative field lists for one visualization.
     */
    private String fieldName;

    public VisualizationFields(String fieldName) {
        this.fieldName = fieldName;
        this.fieldsToVisualize = new HashMap<>();
    }

    public String getFieldName() {
        return fieldName;
    }

    public Map<String, List<DataModelTreeNode>> getFieldsToVisualize() {
        return fieldsToVisualize;
    }

    public void setFieldsToVisualize(Map<String, List<DataModelTreeNode>> fieldsToVisualize) {
        this.fieldsToVisualize = fieldsToVisualize;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }


    /**
     * Adds an array tree to the visualization fields.
     *
     * @param fieldName The name of the fields.
     * @param primitiveTypeOfArray The type of the array contents.
     * @param dimension The dimension of the array (can be multi-dimensional)
     * @return A reference to this {@link VisualizationFields} object for chaining.
     */
    public VisualizationFields addNewArray(String fieldName, IoTDataTypes primitiveTypeOfArray, int dimension) {
        if (!IoTDataTypes.isPrimitive(primitiveTypeOfArray)) {
            // For visualizations only arrays with a primitive type are valid.
            return this;
        }

        DataModelTreeNode typeNode = getNewDataModelTreeNode(fieldName, primitiveTypeOfArray);

        List<DataModelTreeNode> arrayNodes = new ArrayList<>();
        for (int i = 0; i < dimension; i++) {
            DataModelTreeNode nextArrNode  = getNewDataModelTreeNode(fieldName, IoTDataTypes.ARRAY);
            arrayNodes.add(nextArrNode);
        }

        for (int i = 0; i < arrayNodes.size(); i++) {
            // If the last array is reached, add the typeNode as child
            if (i == arrayNodes.size()-1) {
                arrayNodes.get(i).addOneChildren(typeNode);
                typeNode.addParent(arrayNodes.get(i));
            } else if (i == 0) {
                continue;
            } else {
                arrayNodes.get(i).addParent(arrayNodes.get(i-1));
                arrayNodes.get(i-1).addOneChildren(arrayNodes.get(i));
            }
        }

        if (!this.fieldsToVisualize.containsKey(fieldName)) {
            List<DataModelTreeNode> nodeToAddList = new ArrayList<>();
            nodeToAddList.add(arrayNodes.get(0));
            this.fieldsToVisualize.put(fieldName, nodeToAddList);
        } else {
            this.fieldsToVisualize.get(fieldName).add(arrayNodes.get(0));
        }
        return this;
    }

    /**
     * Adds a primitive {@link IoTDataTypes} field to the visualization.
     *
     * @param fieldName The name of the field.
     * @param primitiveType The type of the field.
     * @return this {@link VisualizationFields} for chaining.
     */
    public VisualizationFields addNewPrimitiveType(String fieldName, IoTDataTypes primitiveType) {
        if (!IoTDataTypes.isPrimitive(primitiveType)) {
            return this;
        }

        if (!this.fieldsToVisualize.containsKey(fieldName)) {
            List<DataModelTreeNode> nodeToAddList = new ArrayList<>();
            nodeToAddList.add(getNewDataModelTreeNode(fieldName, primitiveType));
            this.fieldsToVisualize.put(fieldName, nodeToAddList);
        } else {
            this.fieldsToVisualize.get(fieldName).add(getNewDataModelTreeNode(fieldName, primitiveType));
        }
        return this;
    }



    public static DataModelTreeNode getNewDataModelTreeNode(String fieldName, IoTDataTypes type) {
        DataTreeNode node = new DataTreeNode();
        node.setName(fieldName);
        node.setType(type.getName());
        DataModelTreeNode modelNode = new DataModelTreeNode(node);
        modelNode.setType(type);
        modelNode.setName(fieldName);
        return modelNode;
    }

}
