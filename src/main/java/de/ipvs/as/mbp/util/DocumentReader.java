package de.ipvs.as.mbp.util;

import de.ipvs.as.mbp.domain.data_model.DataModelDataType;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;
import org.bson.Document;

import java.util.*;

/**
 * Provides methods to access a {@link Document} based on a given {@link de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree}.
 * This access includes read operations and write operations to modify a given Document.
 */
public class DocumentReader {

    /**
     * Stores array indices to resolve list indices in the {@link Document}. The order of the
     * indices stored in the queue must be the same as it would occur in a json path.
     */
    private final ArrayDeque<Integer> arrayIndexQueue;

    /**
     * Represents a slice of the {@link de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree} in the
     * order from the root to the leaves.
     */
    private final List<DataModelTreeNode> pathList;

    /**
     * Returns a dequeue of array indices of all predecessor nodes of a given {@link DataModelTreeNode}
     * with each index being the maximum possible index according to the
     * {@link de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree} array size information.
     *
     * @param node The DataModelTreeNode for which the max predecessor array indices should be calculated.
     * @return Dequeue with all maximum predecessor array indices in the order from root node.
     */
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
            if (currNode.getType() == DataModelDataType.ARRAY) {
                maxArrayIndexQueue.addFirst(currNode.getSize() - 1);
            }
        }

        return maxArrayIndexQueue;
    }

    /**
     * Creates a new instance to access {@link Document} (read and modify)
     *
     * @param node            The {@link DataModelTreeNode} which represents the data position to retrieve/modify.
     * @param arrayIndexQueue Array indices dequeue to resolve list indices in the {@link Document}. The order of the
     *                        indices stored in the queue must be the same as it would occur in a json path.
     */
    public DocumentReader(DataModelTreeNode node, ArrayDeque<Integer> arrayIndexQueue) {
        // Get all the predecessors of the node and add the node itself to it
        List<DataModelTreeNode> predecessorsOfNode = node.getPredecessors();
        predecessorsOfNode.add(node);

        // Remove the root if existing in the path
        if (predecessorsOfNode.get(0).getParent() == null) {
            predecessorsOfNode.remove(0);
        }

        this.pathList = predecessorsOfNode;

        this.arrayIndexQueue = arrayIndexQueue;
    }

    /**
     * Retrieves all objects of a {@link Document} which correspond to a certain data model tree
     * position, specified by a {@link DataModelTreeNode}
     * (passed with constructor call: e.g. {@link DocumentReader#DocumentReader(DataModelTreeNode, ArrayDeque)}).
     *
     * @param docToRead The document which should be read at a specified position.
     * @return A list of all objects stored in the docToRead which belong to the DataModelTreeNode.
     */
    public List<Object> getValuesByDataModelTreeNode(Document docToRead) {
        List<Object> retObjects = new ArrayList<>();

        //extractNextDataStructure(retObjects, pathQueue, docToRead, -1, arrayIndexQueue, null);
        extractNextDataStructure(retObjects, 0, docToRead, -1, arrayIndexQueue, null);

        return retObjects;
    }

    /**
     * Overwrites the objects at the specified {@link DataModelTreeNode} position of a document with a new object.
     *
     * @param docToRead The document which should be modified.
     * @param newValue  The new object which should replace the object(s) at the specified {@link DataModelTreeNode}
     *                  position (was specified by constructor call).
     * @return A list of all objects which belong to the DataModelTreeNode.
     */
    public List<Object> editValuesByDataModelTreeNode(Document docToRead, Object newValue) {
        List<Object> retObjects = new ArrayList<>();

        extractNextDataStructure(retObjects, 0, docToRead, -1, arrayIndexQueue, newValue);

        return retObjects;
    }

    /**
     * Method used to recursively iterate over a {@link Document} and edit entries of this document at
     * specified positions.
     *
     * @param returnObjects The list which should contain at the end of the recursion all objects at the specified
     *                      DataModelTreeNode position.
     * @param nextNodeIndex Index which acts as pointer for the {@link DocumentReader#pathList} to keep track of the current
     *                      node which is handled.
     * @param currentDataStructureToHandle Data structure which was handled at the last recursion step which is either
     *                                     a single Java object representing a primtive type or an ArrayList.
     * @param arrIndex The next array index to consider.
     * @param arrIndicesQueue The {@link DocumentReader#arrayIndexQueue}.
     * @param newValueForEdit If not null this object will be used to overwrite all objects which are retrieved at the
     *                        specified DataModelTreeNode position.
     */
    private void extractNextDataStructure(List<Object> returnObjects, int nextNodeIndex, Object currentDataStructureToHandle, int arrIndex, ArrayDeque<Integer> arrIndicesQueue, Object newValueForEdit) {
        DataModelTreeNode node = this.pathList.get(nextNodeIndex);

        DataModelTreeNode parent = node.getParent();

        DataModelDataType typeOfNode = node.getType();

        // In the primitive case we are a at the end and we can return the result immediately
        if (DataModelDataType.isPrimitive(typeOfNode)) {

            if (parent.getType() == DataModelDataType.OBJECT) {
                if (newValueForEdit != null) {
                    ((Document) currentDataStructureToHandle).put(node.getName(), newValueForEdit);
                }
                returnObjects.add(((Document) currentDataStructureToHandle).get(node.getName()));
            } else if (parent.getType() == DataModelDataType.ARRAY) {
                if (newValueForEdit != null) {
                    ((List<Object>) currentDataStructureToHandle).set(arrIndex, newValueForEdit);
                }
                returnObjects.add(((List<Object>) currentDataStructureToHandle).get(arrIndex));
            }

        } else if (typeOfNode == DataModelDataType.ARRAY) {

            List<Object> nextDataStructureToHandle = null;

            if (parent.getType() == DataModelDataType.OBJECT) {
                nextDataStructureToHandle = (List<Object>) ((Document) currentDataStructureToHandle).get(node.getName());
            } else if (parent.getType() == DataModelDataType.ARRAY) {
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
                    extractNextDataStructure(returnObjects, nextNodeIndex + 1, nextDataStructureToHandle, i, arrIndicesQueue, newValueForEdit);
                }
            } else {
                // No wildcard
                extractNextDataStructure(returnObjects, nextNodeIndex + 1, nextDataStructureToHandle, chosenAccessIndexForChild, arrIndicesQueue, newValueForEdit);
            }

        } else if (typeOfNode == DataModelDataType.OBJECT) {

            Document nextDataStructureToHandle = null;

            if (parent.getType() == DataModelDataType.OBJECT) {
                nextDataStructureToHandle = (Document) ((Document) currentDataStructureToHandle).get(node.getName());
            } else if (parent.getType() == DataModelDataType.ARRAY) {
                nextDataStructureToHandle = (Document) ((List<Object>) currentDataStructureToHandle).get(arrIndex);
            }

            extractNextDataStructure(returnObjects, nextNodeIndex + 1, nextDataStructureToHandle, -1, arrIndicesQueue, newValueForEdit);
        }
    }

}
