package de.ipvs.as.mbp.util;

import com.jayway.jsonpath.JsonPath;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;
import org.bson.Document;

import java.util.*;

/**
 * Provides methods to access a {@link Document} based on a given {@link de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree}.
 */
public class DocumentUtils {

    private ArrayDeque<DataModelTreeNode> pathQueue;
    private ArrayDeque<Integer> arrayIndexQueue;
    private List<DataModelTreeNode> pathList;

    public DocumentUtils(JsonPath jsonPath) {

    }

    public DocumentUtils(String jsonPath) {

    }

    public static ArrayDeque<Integer> getMaxArrayIndexOfQueueOfNode(DataModelTreeNode node) {
        // Get all the predecessors of the node and add the node itself to it
        List<DataModelTreeNode> predecessorsOfNode = node.getPredecessors();
        predecessorsOfNode.add(node);

        // Remove the root if existing in the path
        if (predecessorsOfNode.get(0).getParent() == null) {
            predecessorsOfNode.remove(0);
        }

        // Find out the array indices and store them to a queue
        ArrayDeque<Integer> maxArrayIndexQueue = new ArrayDeque<>();
        for (int i = predecessorsOfNode.size() - 1; i >= 0; i--) {
            DataModelTreeNode currNode = predecessorsOfNode.get(i);
            if (currNode.getType() == IoTDataTypes.ARRAY) {
                maxArrayIndexQueue.addFirst(currNode.getSize()-1);
            }
        }

        return maxArrayIndexQueue;
    }

    public DocumentUtils(DataModelTreeNode node, ArrayDeque<Integer> arrayIndexQueue) {
        // Get all the predecessors of the node and add the node itself to it
        List<DataModelTreeNode> predecessorsOfNode = node.getPredecessors();
        predecessorsOfNode.add(node);

        // Remove the root if existing in the path
        if (predecessorsOfNode.get(0).getParent() == null) {
            predecessorsOfNode.remove(0);
        }

        this.pathList = predecessorsOfNode;

        // Add nodes to a queue
        this.pathQueue = new ArrayDeque<>();
        for (int i = predecessorsOfNode.size() - 1; i >= 0; i--) {
            pathQueue.addFirst(predecessorsOfNode.get(i));
        }

        this.arrayIndexQueue = arrayIndexQueue;
    }

    public List<Object> getValuesByDataModelTreeNode(Document docToRead) {
        List<Object> retObjects = new ArrayList<>();

        //extractNextDataStructure(retObjects, pathQueue, docToRead, -1, arrayIndexQueue, null);
        extractNextDataStructure(retObjects, 0, docToRead, -1, arrayIndexQueue, null);

        return retObjects;
    }

    public  List<Object> editValuesByDataModelTreeNode(Document docToRead, Object newValue) {
        List<Object> retObjects = new ArrayList<>();

        extractNextDataStructure(retObjects, 0, docToRead, -1, arrayIndexQueue, newValue);

        return retObjects;
    }

    private void extractNextDataStructure(List<Object> returnObjects, int nextNodeIndex, Object currentDataStructureToHandle, int arrIndex, ArrayDeque<Integer> arrIndicesQueue, Object newValueForEdit) {
        DataModelTreeNode node = this.pathList.get(nextNodeIndex);

        DataModelTreeNode parent = node.getParent();

        IoTDataTypes typeOfNode = node.getType();

        // In the primitive case we are a at the end and we can return the result immediately
        if (IoTDataTypes.isPrimitive(typeOfNode)) {

            if (parent.getType() == IoTDataTypes.OBJECT) {
                if (newValueForEdit != null) {
                    ((Document) currentDataStructureToHandle).put(node.getName(), newValueForEdit);
                }
                returnObjects.add(((Document) currentDataStructureToHandle).get(node.getName()));
            } else if (parent.getType() == IoTDataTypes.ARRAY) {
                if (newValueForEdit != null) {
                    ((List<Object>) currentDataStructureToHandle).set(arrIndex, newValueForEdit);
                }
                returnObjects.add(((List<Object>) currentDataStructureToHandle).get(arrIndex));
            }

        } else if (typeOfNode == IoTDataTypes.ARRAY) {

            List<Object> nextDataStructureToHandle = null;

            if (parent.getType() == IoTDataTypes.OBJECT) {
                nextDataStructureToHandle = (List<Object>) ((Document) currentDataStructureToHandle).get(node.getName());
            } else if (parent.getType() == IoTDataTypes.ARRAY) {
                nextDataStructureToHandle = (List<Object>) ((List<Object>) currentDataStructureToHandle).get(arrIndex);
            }

            int chosenAccessIndexForChild = -1;
            if (!arrIndicesQueue.isEmpty()) {
                // Check how to handle array indices in the array indices queue; -1 means: wildcard (equivalent to * in json path)
                chosenAccessIndexForChild = arrIndicesQueue.removeFirst();
            }

            if (chosenAccessIndexForChild < 0) {
                // Wildcard mode

                for (int i = 0; i < node.getSize(); i++) {
                    extractNextDataStructure(returnObjects, nextNodeIndex+1, nextDataStructureToHandle, i, arrIndicesQueue, newValueForEdit);
                }
            } else {
                // No wildcard
                extractNextDataStructure(returnObjects, nextNodeIndex+1, nextDataStructureToHandle, chosenAccessIndexForChild, arrIndicesQueue, newValueForEdit);
            }

        } else if (typeOfNode == IoTDataTypes.OBJECT) {

            Document nextDataStructureToHandle = null;

            if (parent.getType() == IoTDataTypes.OBJECT) {
                nextDataStructureToHandle = (Document) ((Document) currentDataStructureToHandle).get(node.getName());
            } else if (parent.getType() == IoTDataTypes.ARRAY) {
                nextDataStructureToHandle = (Document) ((List<Object>) currentDataStructureToHandle).get(arrIndex);
            }

            extractNextDataStructure(returnObjects, nextNodeIndex+1, nextDataStructureToHandle, -1, arrIndicesQueue, newValueForEdit);
        }
    }

}
