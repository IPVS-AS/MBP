package de.ipvs.as.mbp.domain.data_model.treelogic;

import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.data_model.DataTreeNode;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.util.Validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class is a tree representation for one data model saved in the {@link DataModel}
 * repository. With the help of this tree representation it is possible to handle heterogeneous
 * IoT data.
 * <p>
 * Provided features:
 * - Validate {@link DataModel}s
 */
public class DataModelTree implements Iterable<DataModelTreeNode> {

    private List<DataTreeNode> repoNodeRepresentationList;
    public static DataTreeNode rootRepoRepresentation;

    public static List<DataModelTreeNode> modelNodeList;
    public static DataModelTreeNode rootNodeModel;

    public DataModelTree(List<DataTreeNode> repoNodesToConvert) {
        this.repoNodeRepresentationList = repoNodesToConvert;
    }

    public static boolean validateWholeTree(List<DataTreeNode> repoNodes) throws EntityValidationException {
        // 1. Check if the nodes are ok on their own, not regarding the whole tree structure
        for (DataTreeNode node : repoNodes) {
            validateOneRepoTreeNode(node);
        }

        // Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create, because some fields are invalid.");

        // 2. Make sure that all names are unique
        for (DataTreeNode node : repoNodes) {
            String currNameToCheck = node.getName().toLowerCase();
            int nameOccurenceCount = 0;
            for (DataTreeNode checkAll : repoNodes) {
                if (checkAll.getName().toLowerCase().equals(currNameToCheck)) {
                    nameOccurenceCount++;
                }
                if (nameOccurenceCount >= 2) {
                    exception.addInvalidField("data model tree error", "Node " + node.getName() + " has" +
                            " no unique name.");
                    throw exception;
                }
            }
        }

        // 3. Check that only one rootNode exists
        int noParentsCount = 0;
        List<DataTreeNode> rootCandidates = new ArrayList<>();
        for (DataTreeNode node : repoNodes) {
            if (!node.hasParents()) {
                noParentsCount++;
                rootCandidates.add(node);
            }
        }
        if (noParentsCount != 1) {
            exception.addInvalidField("data model tree error", "Tree is missing a root or has too " +
                    "much roots.");
        } else {
            rootRepoRepresentation = rootCandidates.get(0);
        }

        // Check if root is object
        if (!rootCandidates.get(0).getType().toLowerCase().equals(IoTDataTypes.OBJECT.getValue())) {
            exception.addInvalidField("data model tree error", "Tree root must be an object.");
        }

        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        buildTree(repoNodes);

        return true;
    }

    public static void buildTree(List<DataTreeNode> repoNodes) throws EntityValidationException {

        // Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create, because some fields are invalid.");

        List<DataModelTreeNode> modelNodes = new ArrayList<>();

        // Convert all TreeNodes to DataModelTreeNodes
        for (DataTreeNode node : repoNodes) {
            DataModelTreeNode equivalentModelNode = new DataModelTreeNode(node);
            modelNodes.add(equivalentModelNode);
        }

        // Add for each DataModelTreeNode the corresponding children and their parent
        for (DataModelTreeNode node : modelNodes) {

            for (DataModelTreeNode nodeToCheck : modelNodes) {
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
                exception.addInvalidField(node.getName() + ".parent", "Parent " + node.getRepositoryTreeNode().getParent() +
                        " is not a known node.");
            }
            if (node.getParent() != null && node.getParent() == node) {
                exception.addInvalidField(node.getName() + ".parent", node.getName() +
                        " cannot have itself as a parent.");
            }
            if (node.getChildren().size() != node.getRepositoryTreeNode().getChildren().size()) {
                exception.addInvalidField(node.getName() + ".children", "There are unknown children" +
                        " nodes in the children list.");
            }
            if (node.getChildren().contains(node)) {
                exception.addInvalidField(node.getName() + ".children", "A node cannot be its own child.");
            }
        }

        for (DataModelTreeNode node : modelNodes) {
            if (node.getParent() != null) {
                System.out.print(node.getName() + ": " + node.getParent().getName() + "; children: ");
            } else {
                System.out.print(node.getName() + ": " + "root" + "; children: ");
            }
            for (DataModelTreeNode child : node.getChildren()) {
                System.out.print(child.getName() + "; ");
            }
            System.out.print("\n");
        }

        System.out.println("Level of the tree: " + getTreeLevel(modelNodes));


        // Make sure that level of tree is at most 5
        if (getTreeLevel(modelNodes) > 5) {
            exception.addInvalidField("data model tree", "The level of the tree must be <= 5");
        }

        // Make sure that after an array always a primitive type occurs


        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        // Get root node, it was already checked that only 1 root exists
        for (DataModelTreeNode node : modelNodes) {
            if (node.getParent() == null) {
                rootNodeModel = node;
                break;
            }
        }

        System.out.println("-------ITERATE--------");
        PreOrderIterator it = new PreOrderIterator(rootNodeModel);
        while (it.hasNext()) {
            System.out.println(it.next().getName());
        }

        modelNodeList = modelNodes;
    }

    /**
     * Returns the maximum level of a {@link DataModelTreeNode}. The root counts already as 1.
     *
     * @param allNodes a list of all nodes belonging to the tree
     * @return the number of tree levels (Root > Child > Leaf has the level 3 for example)
     */
    public static int getTreeLevel(List<DataModelTreeNode> allNodes) {
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
            while(nextParent.getParent() != null) {
                levelCount++;
                nextParent = nextParent.getParent();
            }
            levelCountPerLeaf.add(levelCount);
        }

        // Return the maximum
        return Collections.max(levelCountPerLeaf);
    }

    public static void validateOneRepoTreeNode(DataTreeNode nodeToValidate) throws EntityValidationException {
        // Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create, because some fields are invalid.");

        // Is the name null or empty?
        if (Validation.isNullOrEmpty(nodeToValidate.getName())) {
            exception.addInvalidField("node name", "All data model tree nodes need a valid name.");
        }

        // Is the type null or empty?
        if (Validation.isNullOrEmpty(nodeToValidate.getType())) {
            exception.addInvalidField("node type", "All data model tree nodes need a valid type.");
            // Is the type known?
        } else if (IoTDataTypes.getDataTypeWithValue(nodeToValidate.getType().toLowerCase()) == null) {
            exception.addInvalidField("node type", nodeToValidate.getType() + " is not a known type.");
        }

        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        // Make sure that the children list ist at least empty, but not null
        if (nodeToValidate.getChildren() == null) {
            nodeToValidate.setChildren(new ArrayList<>());
        }

        // Are both, parent and children null or empty?
        if ((nodeToValidate.getChildren().size() <= 0)
                && Validation.isNullOrEmpty(nodeToValidate.getParent())) {
            exception.addInvalidField("tree error", "Node " + nodeToValidate.getName() + " is not" +
                    " connected to the tree.");
        }

        // Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        // Check if: type = primitive --> no children
        if (IoTDataTypes.isPrimitive(IoTDataTypes.getDataTypeWithValue(nodeToValidate.getType().toLowerCase()))) {
            if (nodeToValidate.getChildren() != null && nodeToValidate.getChildren().size() > 0) {
                exception.addInvalidField("tree error", "Node " + nodeToValidate.getName() + " is " +
                        "a primitive type but has children.");
            }
        }

        // Check if: type = object --> at least one children
        if (nodeToValidate.getType().toLowerCase().equals(IoTDataTypes.OBJECT.getValue())) {
            if (nodeToValidate.getChildren() == null || nodeToValidate.getChildren().size() <= 0) {
                exception.addInvalidField("tree error", "Node " + nodeToValidate.getName() + " is " +
                        " an object but has no children.");
            }
        }

        // Check if: type = array --> exactly one children and always with a dimension >= 1
        if (nodeToValidate.getType().toLowerCase().equals(IoTDataTypes.ARRAY.getValue())) {
            if (nodeToValidate.getChildren() == null || nodeToValidate.getChildren().size() != 1) {
                exception.addInvalidField("tree error", "Node " + nodeToValidate.getName() + " is " +
                        " an array and needs exactly one child.");
            }
            if (nodeToValidate.getDimension() <= 1) {
                exception.addInvalidField("tree error", "Node " + nodeToValidate.getName() + " is " +
                        " an array and needs a predefined dimension.");
            }
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
}
