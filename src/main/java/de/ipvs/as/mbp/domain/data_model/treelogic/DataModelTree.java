package de.ipvs.as.mbp.domain.data_model.treelogic;

import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.data_model.DataTreeNode;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.util.Validation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class is a tree representation for one data model saved in the {@link DataModel}
 * repository. It is a schema to describe how the IoT data of different components
 * is expected to look like.
 * With the help of this tree representation it is possible to handle heterogeneous
 * IoT data.
 * <p>
 * Provided features:<br>
 * - Validate {@link DataModel}s<br>
 * - Build a tree data structure from a {@link DataModel} using {@link DataModelTreeNode}s as nodes.<br>
 * - Generate a JSON MQTT example message<br>
 */
public class DataModelTree implements Iterable<DataModelTreeNode> {

    /**
     * The original node data from the {@link DataModel} repository.
     */
    private List<DataTreeNode> repoNodeRepresentationList;

    /**
     * The root node of the original node representation
     */
    private DataTreeNode rootRepoRepresentation;

    /**
     * The converted tree nodes of the initial {@link DataModelTree#repoNodeRepresentationList}
     */
    private List<DataModelTreeNode> modelNodeList;

    /**
     * The root node of the tree
     */
    private DataModelTreeNode rootNodeModel;

    /**
     * Builds a tree data structure out of repository {@link DataTreeNode}s. Before the tree is built, various
     * validation steps are proceeded. If the tree is not a proper tree a {@link EntityValidationException} will
     * be thrown.
     *
     * @param repoNodesToConvert All {@link DataTreeNode}s which should be transformed to {@link DataModelTreeNode}s
     *                           and mutually linked forming a tree.
     * @throws EntityValidationException If a validation error occurs.
     */
    public DataModelTree(List<DataTreeNode> repoNodesToConvert) throws EntityValidationException {
        this.repoNodeRepresentationList = repoNodesToConvert;
        this.modelNodeList = new ArrayList<>();
        validateAndBuildTree();

    }

    /**
     * Validates the tree as initially provided in {@link DataModelTree#repoNodeRepresentationList} validates
     * it and converts it to a tree strucutre with {@link DataModelTreeNode}s.
     *
     * @throws EntityValidationException - If a validation error occurs.
     */
    public void validateAndBuildTree() throws EntityValidationException {
        // 1. Check if the nodes are ok on their own, not regarding the whole tree structure
        for (DataTreeNode node : this.repoNodeRepresentationList) {
            validateOneRepoTreeNode(node);
        }

        // Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create, because some fields are invalid.");

        // 2. Make sure that all names are unique
        for (DataTreeNode node : this.repoNodeRepresentationList) {
            String currNameToCheck = node.getName().toLowerCase();
            int nameOccurenceCount = 0;
            for (DataTreeNode checkAll : this.repoNodeRepresentationList) {
                if (checkAll.getName().toLowerCase().equals(currNameToCheck)) {
                    nameOccurenceCount++;
                }
                if (nameOccurenceCount >= 2) {
                    exception.addInvalidField("treeNodes", "Node " + node.getName() + " has" +
                            " no unique name.");
                    throw exception;
                }
            }
        }

        // 3. Check that only one rootNode exists
        int noParentsCount = 0;
        List<DataTreeNode> rootCandidates = new ArrayList<>();
        for (DataTreeNode node : this.repoNodeRepresentationList) {
            if (!node.hasParents()) {
                noParentsCount++;
                rootCandidates.add(node);
            }
        }
        if (noParentsCount != 1) {
            exception.addInvalidField("treeNodes", "Tree is missing a root or has too " +
                    "much roots.");
        } else {
            this.rootRepoRepresentation = rootCandidates.get(0);
            // Check if root is object
            if (!rootCandidates.get(0).getType().toLowerCase().equals(IoTDataTypes.OBJECT.getValue())) {
                exception.addInvalidField("treeNodes", "Tree root must be an object.");
            }
        }

        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        // Now build the tree
        buildTree();

        return;
    }

    public DataModelTreeNode getRoot() {
        return rootNodeModel;
    }

    /**
     * Builds the tree and proceeds validation steps which can only be performed with a whole existing tree.
     *
     * @throws EntityValidationException - If a validation error occurs.
     */
    private void buildTree() throws EntityValidationException {

        // Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create, because some fields are invalid.");

        // Convert all TreeNodes to DataModelTreeNodes
        for (DataTreeNode node : this.repoNodeRepresentationList) {
            DataModelTreeNode equivalentModelNode = new DataModelTreeNode(node);
            this.modelNodeList.add(equivalentModelNode);
        }

        // Add for each DataModelTreeNode the corresponding children and their parent
        for (DataModelTreeNode node : this.modelNodeList) {

            for (DataModelTreeNode nodeToCheck : this.modelNodeList) {
                // Add parent if found
                if (!Validation.isNullOrEmpty(node.getRepositoryTreeNode().getParent())
                        && node.getRepositoryTreeNode().getParent().equals(nodeToCheck.getName())) {
                    node.addParent(nodeToCheck);
                }
                if (node.getRepositoryTreeNode().getChildren().contains(nodeToCheck.getName())) {
                    node.addOneChildren(nodeToCheck);
                }
            }

            if (node.getParent() == null && node.getRepositoryTreeNode().hasParents()) {
                exception.addInvalidField("treeNodes", "Parent " + node.getRepositoryTreeNode().getParent() +
                        " is not a known node.");
            }
            if (node.getParent() != null && node.getParent() == node) {
                exception.addInvalidField("treeNodes", node.getName() +
                        " cannot have itself as a parent.");
            }
            if (node.getChildren().size() != node.getRepositoryTreeNode().getChildren().size()) {
                exception.addInvalidField("treeNodes", "There are unknown children" +
                        " nodes in the children list of node " + node.getName() + ".");
            }
            if (node.getChildren().contains(node)) {
                exception.addInvalidField("treeNodes", "Node " + node.getName() + " cannot be its own child.");
            }
        }

        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        // Get root node, it was already checked that only 1 root exists
        for (DataModelTreeNode node : this.modelNodeList) {
            if (node.getParent() == null) {
                rootNodeModel = node;
                break;
            }
        }

        // Count the tree nodes by traversing in preorder to check if all nodes are accessible from the root node
        int treeNodeCount = 0;
        PreOrderIterator it = new PreOrderIterator(rootNodeModel);

        // Check if the tree is cyclic or in general is not built like a real tree
        if (it.isCyclic()) {
            exception.addInvalidField("treeNodes", "Tree is cyclic or one node has multiple" +
                    " parents.");
        }

        while (it.hasNext()) {
            DataModelTreeNode next = it.next();
            next.updateTreePath();
            treeNodeCount++;
        }

        // Check if the traversed tree has a different number of nodes than the one which should be created to the repo
        if (treeNodeCount != this.repoNodeRepresentationList.size()) {
            exception.addInvalidField("treeNodes", "Tree is not properly traversable.");
        }

        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        // Make sure that level of tree is at most 5
        if (getTreeLevel(this.modelNodeList) > 5) {
            exception.addInvalidField("treeNodes", "The level of the tree must be <= 5");
        }

        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }

    /**
     * Returns the maximum level of a {@link DataModelTreeNode}. The root counts already as 1.
     *
     * @param allNodes a list of all nodes belonging to the tree
     * @return the number of tree levels (Root > Child > Leaf has the level 3 for example)
     */
    public int getTreeLevel(List<DataModelTreeNode> allNodes) {
        // Create a list of all leaf nodes
        List<DataModelTreeNode> leafNodes = new ArrayList<>();
        for (DataModelTreeNode node : allNodes) {
            if (node.isLeaf()) {
                leafNodes.add(node);
            }
        }

        // For each leaf node: Go up to the root and count the occurrences of parents
        List<Integer> levelCountPerLeaf = new ArrayList<>();
        for (DataModelTreeNode leafNode : leafNodes) {
            int levelCount = 1; // The leaf node itself is already counted
            DataModelTreeNode nextParent = leafNode;

            // Go the tree up until the root is reached
            while (nextParent.getParent() != null) {
                levelCount++;
                nextParent = nextParent.getParent();
            }
            levelCountPerLeaf.add(levelCount);
        }

        // Return the maximum
        return Collections.max(levelCountPerLeaf);
    }

    /**
     * Validates a single {@link DataTreeNode}.
     *
     * @param nodeToValidate The {@link DataTreeNode} to validate.
     * @throws EntityValidationException If a validation error occurs.
     */
    private void validateOneRepoTreeNode(DataTreeNode nodeToValidate) throws EntityValidationException {
        // Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create, because some fields are invalid.");

        // Is the name null or empty?
        if (Validation.isNullOrEmpty(nodeToValidate.getName())) {
            exception.addInvalidField("treeNodes", "All data model tree nodes need a valid name.");
        }

        // Is the type null or empty?
        if (Validation.isNullOrEmpty(nodeToValidate.getType())) {
            exception.addInvalidField("treeNodes", "All data model tree nodes need a valid type.");
            // Is the type known?
        } else if (IoTDataTypes.getDataTypeWithValue(nodeToValidate.getType().toLowerCase()) == null) {
            exception.addInvalidField("treeNodes", nodeToValidate.getType() + " is not a known type.");
        }

        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        // Make sure that the children list ist at least empty, but not null
        if (nodeToValidate.getChildren() == null) {
            nodeToValidate.setChildren(new ArrayList<>());
        }

        // Set the dimension to -1 if not an array
        if (IoTDataTypes.getDataTypeWithValue(nodeToValidate.getType().toLowerCase()) != IoTDataTypes.ARRAY) {
            nodeToValidate.setDimension(-1);
        }

        // Are both, parent and children null or empty?
        if ((nodeToValidate.getChildren().size() <= 0)
                && Validation.isNullOrEmpty(nodeToValidate.getParent())) {
            exception.addInvalidField("treeNodes", "Node " + nodeToValidate.getName() + " is not" +
                    " connected to the tree.");
        }

        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        // Check if: type = primitive --> no children
        if (IoTDataTypes.isPrimitive(IoTDataTypes.getDataTypeWithValue(nodeToValidate.getType().toLowerCase()))) {
            if (nodeToValidate.getChildren() != null && nodeToValidate.getChildren().size() > 0) {
                exception.addInvalidField("treeNodes", "Node " + nodeToValidate.getName() + " is " +
                        "a primitive type but has children.");
            }
        }

        // Check if: type = object --> at least one children
        if (nodeToValidate.getType().toLowerCase().equals(IoTDataTypes.OBJECT.getValue())) {
            if (nodeToValidate.getChildren() == null || nodeToValidate.getChildren().size() <= 0) {
                exception.addInvalidField("treeNodes", "Node " + nodeToValidate.getName() + " is " +
                        " an object but has no children.");
            }
        }

        // Check if: type = array --> exactly one children and always with a dimension >= 1
        if (nodeToValidate.getType().toLowerCase().equals(IoTDataTypes.ARRAY.getValue())) {
            if (nodeToValidate.getChildren() == null || nodeToValidate.getChildren().size() != 1) {
                exception.addInvalidField("treeNodes", "Node " + nodeToValidate.getName() + " is " +
                        " an array and needs exactly one child.");
            }
            if (nodeToValidate.getDimension() <= 1) {
                exception.addInvalidField("treeNodes", "Node " + nodeToValidate.getName() + " is " +
                        " an array and needs a predefined dimension.");
            }
        }

        // Make sure that the node has himself not as children
        if (nodeToValidate.getChildren().contains(nodeToValidate.getName())) {
            exception.addInvalidField("treeNodes", "Node " + nodeToValidate.getName() + " is " +
                    " not allowed to have itself as a child.");
        }

        // Make sure that children nodes do not contain the parent node
        if (nodeToValidate.getChildren().contains(nodeToValidate.getParent())) {
            exception.addInvalidField("treeNodes", "Node " + nodeToValidate.getName() + " is " +
                    " not allowed to have a parent which is also a child.");
        }


        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }

    @Override
    public Iterator<DataModelTreeNode> iterator() {
        return new PreOrderIterator(rootNodeModel);
    }

    /**
     * Returns an example MQTT message which will be supported by this {@link DataModelTree}. The example
     * shows an user how the message must be formatted so that the system can handle the data.
     *
     * @return The example JSON string
     */
    public String getJSONExample() {

        String retString = "";

        // Start with root
        JSONObject root = new JSONObject();
        for (DataModelTreeNode node : this.rootNodeModel.getChildren()) {
            getJSONFromChild(node, null, root);
        }
        try {
            retString = "{\"value\": " + root.toString() + "}";
            System.out.println(root.toString(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retString;
    }

    /**
     * Helper method for the {@link DataModelTree#getJSONExample()} method which handles one necessary
     * step for each tree node. The method is called recursively within itself to iterate in Preorder the
     * whole tree.
     *
     * @param currNode
     * @param lastArray A reference to a {@link JSONArray} which should be set to the last occurred JSONArray if
     *                  the last occurred complex JSON data type (array and object) was an array. Otherwise set this
     *                  to null.
     * @param lastObject A reference to a {@link JSONObject} which should be set to the last occurred JSON object if
     *                   the last occurred complex JSON data type (array and object) was an object. Otherwise set this
     *                   to null.
     */
    private void getJSONFromChild(DataModelTreeNode currNode, JSONArray lastArray, JSONObject lastObject) {
        try {
            if (lastArray == null && lastObject != null) {
                // The parent was an object
                if (currNode.getType() == IoTDataTypes.OBJECT) {
                    JSONObject newObj = new JSONObject();
                    lastObject.put(currNode.getName(), newObj);
                    // Call the function for all childs recursively
                    for (DataModelTreeNode node : currNode.getChildren()) {
                        getJSONFromChild(node, null, newObj);
                    }
                } else if (currNode.getType() == IoTDataTypes.ARRAY) {
                    JSONArray newArr = new JSONArray();
                    lastObject.put(currNode.getName(), newArr);
                    // Call the function for the childs recursively (call it that often like dimensions)
                    for (int i = 0; i < currNode.getDimension(); i++) {
                        System.out.println(currNode.getDimension());
                        getJSONFromChild(currNode.getChildren().get(0), newArr, null);
                    }
                } else if (currNode.getType() == IoTDataTypes.DECIMAL128
                        || currNode.getType() == IoTDataTypes.DOUBLE) {
                    lastObject.put(currNode.getName(), 14.25);
                    // As primitive no children to be expected --> return
                } else if (currNode.getType() == IoTDataTypes.LONG) {
                    lastObject.put(currNode.getName(), 20);
                } else if (currNode.getType() == IoTDataTypes.BOOLEAN) {
                    lastObject.put(currNode.getName(), true);
                    // As primitive no children to be expected --> return
                } else if (currNode.getType() == IoTDataTypes.INT) {
                    lastObject.put(currNode.getName(), 4);
                    // As primitive no children to be expected --> return
                } else if (currNode.getType() == IoTDataTypes.STRING) {
                    lastObject.put(currNode.getName(), "String");
                    // As primitive no children to be expected --> return
                } else if (currNode.getType() == IoTDataTypes.DATE) {
                    lastObject.put(currNode.getName(), "Date");
                    // As primitive no children to be expected --> return
                } else if (currNode.getType() == IoTDataTypes.BINARY) {
                    lastObject.put(currNode.getName(), "Binary");
                    // As primitive no children to be expected --> return
                }


            } else if (lastArray != null && lastObject == null) {
                // The parent was an array
                if (currNode.getType() == IoTDataTypes.OBJECT) {
                    JSONObject newObj = new JSONObject();
                    lastArray.put(newObj);
                    // Call the function for all childs recursively
                    for (DataModelTreeNode node : currNode.getChildren()) {
                        getJSONFromChild(node, null, newObj);
                    }
                } else if (currNode.getType() == IoTDataTypes.ARRAY) {
                    JSONArray newArr = new JSONArray();
                    lastArray.put(newArr);
                    // Call the function for the childs recursively (call it that often like dimensions)
                    for (int i = 0; i < currNode.getDimension(); i++) {
                        getJSONFromChild(currNode.getChildren().get(0), newArr, null);
                    }
                } else if (currNode.getType() == IoTDataTypes.DECIMAL128
                        || currNode.getType() == IoTDataTypes.DOUBLE) {
                    lastArray.put(14.25);
                    // As primitive no children to be expected --> return
                } else if (currNode.getType() == IoTDataTypes.LONG) {
                    lastObject.put(currNode.getName(), 20);
                } else if (currNode.getType() == IoTDataTypes.BOOLEAN) {
                    lastArray.put(true);
                    // As primitive no children to be expected --> return
                } else if (currNode.getType() == IoTDataTypes.INT) {
                    lastArray.put(4);
                    // As primitive no children to be expected --> return
                } else if (currNode.getType() == IoTDataTypes.STRING) {
                    lastArray.put("String");
                    // As primitive no children to be expected --> return
                }  else if (currNode.getType() == IoTDataTypes.DATE) {
                    lastArray.put("Date");
                    // As primitive no children to be expected --> return
                }  else if (currNode.getType() == IoTDataTypes.BINARY) {
                    lastArray.put("Binary");
                    // As primitive no children to be expected --> return
                }
            }
        } catch (JSONException e) {
            // TODO handle this properly
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        String retString = "";
        PreOrderIterator it = new PreOrderIterator(rootNodeModel);
        while (it.hasNext()) {
            DataModelTreeNode next = it.next();
            retString += next.getTreePath() + " " + next.getName() + "\n";
        }
        return retString;
    }
}
