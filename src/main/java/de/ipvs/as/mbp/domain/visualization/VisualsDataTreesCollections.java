package de.ipvs.as.mbp.domain.visualization;

import de.ipvs.as.mbp.domain.data_model.DataTreeNode;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;

import java.util.Arrays;
import java.util.HashMap;

public class VisualsDataTreesCollections {

    private static HashMap<String, Visualization> visMap;

    public VisualsDataTreesCollections() {
        visMap = new HashMap<>();

        // ----- Boolean visualization ------ //
        DataTreeNode node = new DataTreeNode();
        node.setName("boolVal");
        node.setType("boolean");
        visMap.put("bool1", new Visualization(Arrays.asList(
                new DataModelTreeNode(node)
        )).setId("bool1").setName("bool1"));

        // ----- String visualization ------ //
        DataTreeNode stringNode = new DataTreeNode();
        stringNode.setName("stringVal");
        stringNode.setType("string");
        visMap.put("string1", new Visualization(Arrays.asList(
                new DataModelTreeNode(stringNode)
        )).setId("string1").setName("string1"));

        // ----- Long array with 5 dimensions visualization ------ //
        DataTreeNode node3 = new DataTreeNode();
        node3.setName("nameOfLong");
        node3.setType("long");

        DataTreeNode node2 = new DataTreeNode();
        node2.setName("arr5Long");
        node2.setType("array");
        node2.setDimension(5);
        node2.setChildren(Arrays.asList(node3.getName()));

        DataModelTreeNode dMNode1 = new DataModelTreeNode(node3);

        DataModelTreeNode dMNode2 = new DataModelTreeNode(node2);
        dMNode2.addOneChildren(dMNode1);
        visMap.put("arr5Long", new Visualization(Arrays.asList(
                dMNode2
        )).setId("arr5Long").setName("arr5Long"));

        // ----- Test example vis ------ //
        DataTreeNode ex1 = new DataTreeNode();
        ex1.setName("x");
        ex1.setType("string");
        DataModelTreeNode dex1 = new DataModelTreeNode(ex1);

        DataTreeNode ex2 = new DataTreeNode();
        ex2.setName("y");
        ex2.setType("String");
        DataModelTreeNode dex2 = new DataModelTreeNode(ex2);

        DataTreeNode exRoot = new DataTreeNode();
        exRoot.setName("z");
        exRoot.setType("object");
        exRoot.setChildren(Arrays.asList(ex1.getName(), ex2.getName()));
        DataModelTreeNode dexRoot = new DataModelTreeNode(exRoot);
        dexRoot.addOneChildren(dex1);
        dexRoot.addOneChildren(dex2);
        ex1.setParent(exRoot.getName());
        ex2.setParent(exRoot.getName());
        dex1.addParent(dexRoot);
        dex2.addParent(dexRoot);

        visMap.put("z", new Visualization(Arrays.asList(
                dexRoot
        )).setId("z").setName("z"));

        // ----- Second Test example vis ------ //
        DataTreeNode xa = new DataTreeNode();
        xa.setName("xa");
        xa.setType("string");
        DataModelTreeNode dxa = new DataModelTreeNode(xa);

        DataTreeNode xb = new DataTreeNode();
        xb.setName("xb");
        xb.setType("string");
        DataModelTreeNode dxb = new DataModelTreeNode(xb);

        DataTreeNode ya = new DataTreeNode();
        ya.setName("ya");
        ya.setType("string");
        DataModelTreeNode dya = new DataModelTreeNode(ya);

        DataTreeNode yb = new DataTreeNode();
        yb.setName("yb");
        yb.setType("string");
        DataModelTreeNode dyb = new DataModelTreeNode(yb);

        DataTreeNode x = new DataTreeNode();
        x.setName("x");
        x.setType("object");
        DataModelTreeNode dx = new DataModelTreeNode(x);

        DataTreeNode y = new DataTreeNode();
        y.setName("y");
        y.setType("object");
        DataModelTreeNode dy = new DataModelTreeNode(y);

        DataTreeNode newRoot = new DataTreeNode();
        newRoot.setName("newRoot");
        newRoot.setType("object");
        DataModelTreeNode dnewRoot = new DataModelTreeNode(newRoot);

        x.setParent(newRoot.getName());
        y.setParent(newRoot.getName());
        xa.setParent(x.getName());
        xb.setParent(x.getName());
        ya.setParent(y.getName());
        yb.setParent(y.getName());

        dx.addParent(dnewRoot);
        dy.addParent(dnewRoot);
        dnewRoot.addOneChildren(dx);
        dnewRoot.addOneChildren(dy);

        dxa.addParent(dx);
        dxb.addParent(dx);
        dx.addOneChildren(dxa);
        dx.addOneChildren(dxb);

        dya.addParent(dy);
        dyb.addParent(dy);
        dy.addOneChildren(dya);
        dy.addOneChildren(dyb);

        visMap.put("newRoot", new Visualization(Arrays.asList(
                dnewRoot
        )).setId("newRoot").setName("newRoot"));
    }

    public Visualization getVisById(String id) {
        return visMap.get(id);
    }
}
