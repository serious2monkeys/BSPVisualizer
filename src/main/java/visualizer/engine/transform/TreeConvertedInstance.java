package visualizer.engine.transform;

import visualizer.engine.Plane;
import visualizer.engine.Vector3d;
import visualizer.engine.solid.LineSegment;
import visualizer.engine.solid.SolidTreeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static visualizer.engine.Plane.*;

/**
 * Created by Anton Doronin on 04.03.2017.
 */
public class TreeConvertedInstance {
    private static double SOLID_MARKER = -1;
    private static List<Double> leafPlane = Arrays.asList(0.0, 0.0, 0.0, Double.MIN_VALUE);
    private static List<Integer> leafNodes = Arrays.asList(-1, -1);
    private ArrayList<Integer> nodes;
    private ArrayList<Double> planeValues;
    private long cells = 0;
    private int[] nodesArray;
    private double[] planesArray;

    public long countCells() {
        return cells;
    }

    public int[] getNodesArray() {
        return nodesArray;
    }

    public double[] getPlanesArray() {
        return planesArray;
    }

    public TreeConvertedInstance(SolidTreeNode treeNode) {
        nodes = new ArrayList<>();
        planeValues = new ArrayList<>();
        operateNode(treeNode, -1);
        serializePlane(treeNode);
        nodesArray = nodes.stream().mapToInt(Integer::intValue).toArray();
        planesArray = planeValues.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private int operateNode(SolidTreeNode node, int index) {
        if (node.isLeaf()) {
            nodes.addAll(leafNodes);
        } else {
            int front = operateNode(node.getFront(), index);
            serializePlane(node.getFront());
            int back = operateNode(node.getBack(), front);
            serializePlane(node.getBack());
            nodes.add(front);
            nodes.add(back);
            index = back;
        }
        cells++;
        index++;
        return index;
    }

    private void serializePlane(SolidTreeNode node) {
        if (node.isLeaf()) {
            planeValues.addAll(leafPlane);
            if (node.isSolid()) {
                planeValues.set(planeValues.size() - 1, SOLID_MARKER);
            }
            return;
        }
        Plane plane = node.getPlane();
        planeValues.addAll(Arrays.asList(plane.normal.x, plane.normal.y, plane.normal.z, plane.dist));
    }

    public Vector3d checkIntersection(LineSegment segment) {
        return checkIntersection(segment, cells - 1);
    }

    public double[] checkIntersection(double[] begin, double[] end) {
        return checkIntersection(begin, end, cells - 1);
    }

    public double[] checkIntersection(double[] begin, double[] end, long cellIndex) {
        int[] cell = extractCell(cellIndex);
        double[] plane = extractPlane(cellIndex);
        if (cell[0] == -1 && cell[1] == -1) {
            if (plane[3] == SOLID_MARKER) {
                return begin;
            }
            return null;
        } else {
            int polygonType = 0;
            List<Integer> types = new ArrayList<>(2);

            double t = (new Vector3d(plane[0], plane[1], plane[2])).dot(new Vector3d(begin[0], begin[1], begin[2])) - plane[3];
            int type = (t < -Plane.EPSILON) ? BACK : (t > Plane.EPSILON) ? FRONT : COPLANAR;
            polygonType |= type;
            types.add(type);

            t = (new Vector3d(plane[0], plane[1], plane[2])).dot(new Vector3d(end[0], end[1], end[2])) - plane[3];
            type = (t < -Plane.EPSILON) ? BACK : (t > Plane.EPSILON) ? FRONT : COPLANAR;
            polygonType |= type;
            types.add(type);

            switch (polygonType) {
                case COPLANAR:
                    return null;
                case FRONT:
                    //System.out.println(" -> front");
                    return checkIntersection(begin, end, cell[0]);
                case BACK:
                    //System.out.println(" -> back");
                    return checkIntersection(begin, end, cell[1]);
                case SPANNING:
                    double[] inters = findIntersection(begin, end, plane);

                    double[][] frontPart = types.get(0) == FRONT
                            ? new double[][]{begin, inters}
                            : new double[][]{inters, end};
                    double[][] backPart = types.get(0) == BACK
                            ? new double[][]{begin, inters}
                            : new double[][]{inters, end};

                    int nearestChild = planesArray[(cell[0] << 2) + 3] < planesArray[(cell[1] << 2) + 3]
                            ? cell[0] : cell[1];
                    double[] test = nearestChild == cell[0]
                            ? checkIntersection(frontPart[0], frontPart[1], cell[0])
                            : checkIntersection(backPart[0], backPart[1], cell[1]);
                    if (test != null) {
                        return test;
                    }
                    return nearestChild == cell[0]
                            ? checkIntersection(backPart[0], backPart[1], cell[1])
                            : checkIntersection(frontPart[0], frontPart[1], cell[0]);
            }
        }
        return null;
    }

    public Vector3d checkIntersection(LineSegment segment, long cellIndex) {
        int[] cell = extractCell(cellIndex);
        double[] plane = extractPlane(cellIndex);
        if (cell[0] == -1 && cell[1] == -1) {
            if (plane[3] == SOLID_MARKER) {
                return segment.getBegin();
            }
            return null;
        } else {
            int polygonType = 0;
            List<Integer> types = new ArrayList<>(2);
            for (Vector3d point : segment.getPoints()) {
                double t = (new Vector3d(plane[0], plane[1], plane[2])).dot(point) - plane[3];
                int type = (t < -Plane.EPSILON) ? BACK : (t > Plane.EPSILON) ? FRONT : COPLANAR;
                polygonType |= type;
                types.add(type);
            }
            switch (polygonType) {
                case COPLANAR:
                    return null;
                case FRONT:
                    //System.out.println(" -> front");
                    return checkIntersection(segment, cell[0]);
                case BACK:
                    //System.out.println(" -> back");
                    return checkIntersection(segment, cell[1]);
                case SPANNING:
                    Vector3d inters = findIntersection(segment, plane);

                    LineSegment frontPart = types.get(0) == FRONT
                            ? new LineSegment(segment.getBegin(), inters)
                            : new LineSegment(inters, segment.getEnd());
                    LineSegment backPart = types.get(0) == BACK
                            ? new LineSegment(segment.getBegin(), inters)
                            : new LineSegment(inters, segment.getEnd());

                    int nearestChild = planesArray[(cell[0] << 2) + 3] < planesArray[(cell[1] << 2) + 3]
                            ? cell[0] : cell[1];
                    Vector3d test = nearestChild == cell[0]
                            ? checkIntersection(frontPart, cell[0])
                            : checkIntersection(backPart, cell[1]);
                    if (test != null) {
                        return test;
                    }
                    return nearestChild == cell[0]
                            ? checkIntersection(backPart, cell[1])
                            : checkIntersection(frontPart, cell[0]);
            }
        }
        return null;
    }

    double[] findIntersection(double[] begin, double[] end, double[] plane) {
        double m = end[0] - begin[0];
        double n = end[1] - begin[1];
        double p = end[2] - begin[2];

        double t = (dotProduct(plane, begin) - plane[3]) /
                (dotProduct(plane, new double[]{m, n, p})) * (-1);
        return new double[]{begin[0] + m * t, begin[1] + n * t, begin[2] + p * t};
    }

    double dotProduct(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    Vector3d findIntersection(LineSegment segment, double[] plane) {
        double m = segment.getEnd().x - segment.getBegin().x;
        double n = segment.getEnd().y - segment.getBegin().y;
        double p = segment.getEnd().z - segment.getBegin().z;

        Vector3d normal = new Vector3d(plane[0], plane[1], plane[2]);
        double t = (normal.dot(segment.getBegin()) - plane[3]) /
                (dotProduct(plane, new double[]{m, n, p})) * (-1);
        return Vector3d.xyz(segment.getBegin().x + m * t, segment.getBegin().y + n * t, segment.getBegin().z + p * t);
    }

    private int[] extractCell(long cellIndex) {
        return new int[]{nodesArray[(int) (cellIndex * 2)], nodesArray[(int) (cellIndex * 2 + 1)]};
    }

    private double[] extractPlane(long cellIndex) {
        return new double[]{planesArray[(int) (cellIndex << 2)],
                planesArray[(int) (cellIndex << 2) + 1],
                planesArray[(int) (cellIndex << 2) + 2],
                planesArray[(int) (cellIndex << 2) + 3]};
    }
}
