package de.ipvs.as.mbp.domain.data_model.treelogic;

import com.sun.xml.bind.v2.util.FatalAdapter;
import org.apache.commons.collections.ArrayStack;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Implementation idea from https://www.geeksforgeeks.org/iterative-preorder-traversal-of-a-n-ary-tree/
 */
public class PreOrderIterator implements Iterator<DataModelTreeNode> {

    private DataModelTreeNode root;
    private List<DataModelTreeNode> stack;
    private List<DataModelTreeNode> visitedNodes;

    public PreOrderIterator(DataModelTreeNode root) {
        this.root = root;
        this.stack = new ArrayList();
        this.stack.add(root);
        this.visitedNodes = new ArrayList<>();
        this.visitedNodes.add(root);

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
                // Is the child unvisited?
                if (!visitedNodes.contains(child)) {
                    // Yes, it is unvisited! Then visit it know by adding it to the stack and to the visitedNodes list.
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

    @Override
    public boolean hasNext() {
        if (visitedNodes.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DataModelTreeNode next() {
        DataModelTreeNode nextElement = visitedNodes.remove(0);
        return nextElement;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported.");
    }
}
