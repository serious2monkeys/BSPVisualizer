package visualizer.engine.solid;

import visualizer.engine.Vector3d;

/**
 * Created by Anton Doronin on 29.01.2017.
 * Представление отрезка прямой в трёхмерном пространстве
 */
public class LineSegment {
    private Vector3d begin, end;

    public LineSegment(Vector3d begin, Vector3d end) {
        this.begin = begin;
        this.end = end;
    }

    public Vector3d getBegin() {
        return begin;
    }

    public Vector3d getEnd() {
        return end;
    }

    public Vector3d[] getPoints() {
        return new Vector3d[] {begin, end};
    }
}
