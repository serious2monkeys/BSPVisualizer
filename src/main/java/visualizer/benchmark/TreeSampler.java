package visualizer.benchmark;

import visualizer.engine.Plane;
import visualizer.engine.Vector3d;
import visualizer.engine.solid.SolidTreeNode;

/**
 * Created by Anton Doronin on 04.04.2017.
 */
public class TreeSampler {

    /**
     * Получение дерева, лучи для которого будут когерентными
     *
     * @return
     */
    public static SolidTreeNode getCoherentTree() {
        SolidTreeNode treeNode = new SolidTreeNode();
        treeNode.setPlane(new Plane(new Vector3d(0, 0, -1), 512));
        buildCoherent(treeNode, 1024);
        return treeNode;
    }

    /**
     * Построение дерева, лучи при трассировке которого, будут когерентными
     *
     * @param node  - узел
     * @param depth - глубина
     */
    private static void buildCoherent(SolidTreeNode node, int depth) {
        if (depth > 1) {
            SolidTreeNode frontNode = new SolidTreeNode();
            frontNode.setPlane((depth / 2) > 1
                    ? new Plane(Vector3d.xyz(0, 0, -1), 512 - depth / 2)
                    : null);
            node.setFront(frontNode);
            buildCoherent(frontNode, depth / 2);
            SolidTreeNode backNode = new SolidTreeNode();
            backNode.setPlane((depth / 2) > 1
                    ? new Plane(Vector3d.xyz(0, 0, -1), 512 + depth / 2)
                    : null);
            node.setBack(backNode);
            buildCoherent(backNode, depth / 2);
            if ((depth / 2) == 1) {
                backNode.setSolid(true);
            }
        }
    }

    /**
     * Возврат сбалансированного дерева
     *
     * @return
     */
    public static SolidTreeNode getBalancedTree() {
        SolidTreeNode node = new SolidTreeNode();
        node.setPlane(new Plane(new Vector3d(0, 0, 1), 0));
        SolidTreeNode frontX = new SolidTreeNode();
        frontX.setPlane(new Plane(new Vector3d(1, 0, 0), 0));
        SolidTreeNode backX = new SolidTreeNode();
        backX.setPlane(new Plane(new Vector3d(-1, 0, 0), 0));
        node.setFront(frontX);
        node.setBack(backX);

        SolidTreeNode fourth = new SolidTreeNode();
        fourth.setPlane(new Plane(new Vector3d(Math.sqrt(2)/2, 0, -Math.sqrt(2)/2), 0));
        frontX.setFront(fourth);
        SolidTreeNode fifth = new SolidTreeNode();
        fifth.setPlane(new Plane(new Vector3d(Math.sqrt(2)/2, 0, Math.sqrt(2)/2),0));
        frontX.setBack(fifth);

        SolidTreeNode sixth = new SolidTreeNode();
        sixth.setPlane(new Plane(new Vector3d(-Math.sqrt(2)/2, 0, Math.sqrt(2)/2), 0));
        backX.setFront(sixth);

        SolidTreeNode seventh = new SolidTreeNode();
        seventh.setPlane(new Plane(new Vector3d(-Math.sqrt(2)/2, 0, - Math.sqrt(2)/2), 0));

        backX.setBack(seventh);

        SolidTreeNode aNode = new SolidTreeNode();
        aNode.setPlane(new Plane(new Vector3d(Math.sin(Math.toRadians(67.5)), 0,
                Math.toRadians(Math.cos(Math.toRadians(67.5)))), 1));

        aNode.setFront(new SolidTreeNode());
        aNode.setBack(new SolidTreeNode());
        aNode.getBack().setSolid(true);

        fourth.setFront(aNode);

        SolidTreeNode bNode = new SolidTreeNode();
        bNode.setPlane(new Plane(new Vector3d(Math.sin(Math.toRadians(22.5)), 0, Math.cos(Math.toRadians(22.5))), 1));
        bNode.setFront(new SolidTreeNode());
        bNode.setBack(new SolidTreeNode());
        bNode.getBack().setSolid(true);

        fourth.setBack(bNode);

        SolidTreeNode cNode = new SolidTreeNode();
        cNode.setPlane(new Plane(new Vector3d(-Math.sin(Math.toRadians(22.5)), 0, Math.cos(Math.toRadians(22.5))), 1));
        cNode.setFront(new SolidTreeNode());
        cNode.setBack(new SolidTreeNode());
        cNode.getBack().setSolid(true);

        fifth.setFront(cNode);

        SolidTreeNode dNode = new SolidTreeNode();
        dNode.setPlane(new Plane(new Vector3d(-Math.sin(Math.toRadians(67.5)), 0, Math.cos(Math.toRadians(67.5))), 1));
        dNode.setFront(new SolidTreeNode());
        dNode.setBack(new SolidTreeNode());
        dNode.getBack().setSolid(true);

        fifth.setBack(dNode);

        SolidTreeNode eNode = new SolidTreeNode();
        eNode.setPlane(new Plane(new Vector3d(-Math.cos(Math.toRadians(22.5)), 0, -Math.sin(Math.toRadians(22.5))),1));
        eNode.setFront(new SolidTreeNode());
        eNode.setBack(new SolidTreeNode());
        eNode.getBack().setSolid(true);

        sixth.setFront(eNode);

        SolidTreeNode fNode = new SolidTreeNode();
        fNode.setPlane(new Plane(new Vector3d(-Math.cos(Math.toRadians(67.5)), 0, -Math.sin(Math.toRadians(67.5))), 1));
        fNode.setFront(new SolidTreeNode());
        fNode.setBack(new SolidTreeNode());
        fNode.getBack().setSolid(true);

        sixth.setBack(fNode);

        SolidTreeNode gNode = new SolidTreeNode();
        gNode.setPlane(new Plane(new Vector3d(Math.cos(Math.toRadians(67.5)),0, -Math.sin(Math.toRadians(67.5))),1));
        gNode.setFront(new SolidTreeNode());
        gNode.setBack(new SolidTreeNode());
        gNode.getBack().setSolid(true);

        seventh.setFront(gNode);

        SolidTreeNode hNode = new SolidTreeNode();
        hNode.setPlane(new Plane(new Vector3d(Math.cos(Math.toRadians(22.5)),0, -Math.sin(Math.toRadians(22.5))),1));
        hNode.setFront(new SolidTreeNode());
        hNode.setBack(new SolidTreeNode());
        hNode.getBack().setSolid(true);

        seventh.setBack(hNode);

        return node;
    }
}
