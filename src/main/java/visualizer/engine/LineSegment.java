package visualizer.engine;

/**
 * Created by Anton Doronin on 29.01.2017.
 * Представление отрезка прямой в трёхмерном пространстве
 */
public class LineSegment {
    private Vector3d[] points = new Vector3d[2];

    public LineSegment(Vector3d begin, Vector3d end) {
        points[0] = begin;
        points[1] = end;
    }

    public Vector3d getBegin() {
        return points[0];
    }

    public Vector3d getEnd() {
        return points[1];
    }

    public Vector3d[] getPoints() {
        return points;
    }
}
