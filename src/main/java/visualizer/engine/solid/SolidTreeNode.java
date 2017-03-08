package visualizer.engine.solid;


import visualizer.engine.Plane;
import visualizer.engine.Polygon;
import visualizer.engine.Vector3d;

import java.util.ArrayList;
import java.util.List;

import static visualizer.engine.Plane.*;
import static visualizer.engine.solid.PlaneUtils.findIntersection;


/**
 * Created by Anton Doronin on 18.02.2017.
 * Узел BSP-дерева, не хранящий геометрии,
 * но имеющий определнный флаг заполненности пространства
 */
public class SolidTreeNode {
    private Plane plane;
    private boolean solid = false;
    private SolidTreeNode front, back;

    public SolidTreeNode(List<Polygon> polygons) {
        if (polygons != null) {
            build(polygons);
        }
    }

    public SolidTreeNode getFront() {
        return front;
    }

    public SolidTreeNode getBack() {
        return back;
    }

    public Plane getPlane() {
        return plane;
    }

    public SolidTreeNode() {

    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isLeaf() {
        return plane == null;
    }

    public final void build(List<Polygon> polygons) {
        if (polygons.isEmpty()) {
            return;
        }
        if (this.plane == null) {
            Polygon chosen = PlaneUtils.choosePlane(polygons);
            this.plane = chosen.plane.clone();
            polygons.remove(chosen);
        }

        List<Polygon> frontP = new ArrayList<>();
        List<Polygon> backP = new ArrayList<>();

        polygons.stream().forEach((polygon) -> this.plane.splitPolygon(polygon, frontP, backP));

        if (this.front == null) {
            this.front = new SolidTreeNode();
        }
        if (frontP.size() > 0) {
            this.front.build(frontP);
        }
        if (this.back == null) {
            this.back = new SolidTreeNode();
        }
        if (backP.size() > 0) {
            this.back.build(backP);
        }
        if (backP.size() == 0) {
            this.back.solid = true;
        }
    }


    /**
     * Проверка на пересечение сцены отрезком
     *
     * @param segment
     * @return
     */
    public Vector3d checkIntersection(LineSegment segment) {
        if (isLeaf()) {
            if (isSolid()) {
                return segment.getBegin();
            }
            return null;
        } else {
            int polygonType = 0;
            List<Integer> types = new ArrayList<>(2);
            for (Vector3d point: segment.getPoints()) {
                double t = this.plane.normal.dot(point) - this.plane.dist;
                int type = (t < -Plane.EPSILON) ? BACK : (t > Plane.EPSILON) ? FRONT : COPLANAR;
                polygonType |= type;
                types.add(type);
            }
            //System.out.println("> switching");
            // Put the polygon in the correct list, splitting it when necessary.
            switch (polygonType) {
                case COPLANAR:
                    return segment.getBegin();
                case FRONT:
                    //System.out.println(" -> front");
                    return front.checkIntersection(segment);
                case BACK:
                    //System.out.println(" -> back");
                    return back.checkIntersection(segment);
                case SPANNING:
                    Vector3d inters = findIntersection(segment, plane);

                    LineSegment frontPart =   types.get(0) == FRONT
                            ? new LineSegment(segment.getBegin(), inters)
                            : new LineSegment(inters, segment.getEnd());
                    LineSegment backPart = types.get(0) == BACK
                            ? new LineSegment(segment.getBegin(), inters)
                            : new LineSegment(inters, segment.getEnd());

                    SolidTreeNode nearest = getNearestChild();
                    Vector3d test = nearest == front
                            ? front.checkIntersection(frontPart)
                            : back.checkIntersection(backPart);
                    if (test != null) {
                        return test;
                    }
                    return nearest == front
                            ? back.checkIntersection(backPart)
                            : front.checkIntersection(frontPart);
            }
            return null;
        }
    }

    private SolidTreeNode getNearestChild() {
        if (front.plane != null && back.plane != null) {
            return front.plane.dist < back.plane.dist ? front : back;
        }
        return front.plane == null ? back : front;
    }
}
