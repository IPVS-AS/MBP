package de.ipvs.as.mbp.domain.data_model.treelogic;

import java.util.*;

/**
 * Preorder iterator for a {@link DataModelTree} using {@link DataModelTreeNode}s. Beyond the pure iteration
 * it provides the opportunity to checker whether the is cyclic or a node has multiple parents and is therefore
 * not a proper tree. This can be checked by using {@link PreOrderIterator#isCyclic()} direct after constructor
 * call.<br>
 *
 * <i>Basic implementation idea from https://www.geeksforgeeks.org/iterative-preorder-traversal-of-a-n-ary-tree/</i>
 */
public class PreOrderIterator implements Iterator<DataModelTreeNode> {

    private DataModelTreeNode root;
    private List<DataModelTreeNode> stack;
    private List<DataModelTreeNode> visitedNodes;

    private boolean isCyclic;

    /**
     * Inits the iterator. If you want to check if the tree is valid (no multiple parents, no cycles) then you
     * can use the {@link PreOrderIterator#isCyclic()} method afterwards.
     *
     * @param root The root of the tree. This is the start point of iteration.
     */
    public PreOrderIterator(DataModelTreeNode root) {
        this.root = root;
        this.stack = new ArrayList();
        this.stack.add(root);
        this.visitedNodes = new ArrayList<>();
        this.visitedNodes.add(root);
        this.isCyclic = false;

        while(stack.size() > 0) {
            boolean allChildrenVisited = false;

            DataModelTreeNode parentNode = null;

            // If the top of the stack is a leaf node, remove it from the stack
            if (stack.get(stack.size()-1).isLeaf()) {
                stack.remove(stack.size()-1);
                continue;
            } else {
                // Top of the stack is parent with children
                parentNode = stack.get(stack.size()-1);
            }

            // Handle the next unvisited child node of the parent
            for (DataModelTreeNode child : parentNode.getChildren()) {
                if (visitedNodes.contains(child)) {
                    if (child.getParent() != parentNode) {
                        this.isCyclic = true;
                    }
                }
                // Is the child unvisited?
                if (!visitedNodes.contains(child)) {
                    // Yes, it is unvisited! Then visit it now by adding it to the stack and to the visitedNodes list.
                    allChildrenVisited = true;
                    stack.add(child);
                    visitedNodes.add(child);
                    // Break to start from the beginning and to explore at first this new added child (if not a leaf)
                    break;
                }
            }

            // As soon as all children of a parent are visited, the parent can be removed from the stack
            if (allChildrenVisited == false) {
                stack.remove(stack.size()-1);
            }
        }
    }

    private boolean hasListDuplicates(List<DataModelTreeNode> listToCheck) {
        Set<DataModelTreeNode> set = new HashSet<>();
        set.addAll(listToCheck);
        return listToCheck.size() != set.size();
    }

    /**
     * @return True if the tree has multiple parents and is therefore not a proper tree (could be cyclic if
     * no multiple parents).
     */
    public boolean isCyclic() {
        return isCyclic;
    }

    @Override
    public boolean hasNext() {
        return visitedNodes.size() > 0;
    }

    @Override
    public DataModelTreeNode next() {
        return visitedNodes.remove(0);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported.");
    }
}
