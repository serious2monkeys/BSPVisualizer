package visualizer;


import com.jogamp.opengl.*;
import visualizer.engine.Bounds;
import visualizer.engine.CSG;
import visualizer.engine.Polygon;
import visualizer.engine.Vector3d;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.stream.Stream;

public class GLRenderer implements GLEventListener {

    private CSG scene;

    private Vector3d rotate = new Vector3d(0, 0, 0);

    private List<Vector3d> points;

    double bounds = 1;

    public GLRenderer() {
        scene = null;
    }

    public GLRenderer(CSG scene) {
        this.scene = scene;
    }

    public GLRenderer(double bounds, List<Vector3d> points) {
        this.points = points;
        this.bounds = bounds;
    }

    public void setRotate(Vector3d rotate) {
        this.rotate = rotate;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0f);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_BLEND);
        gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL.GL_NICEST);
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glPointSize(2f);
        gl.glLineWidth(1.0f);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2ES1 gl = drawable.getGL().getGL2ES1();
        //GLU glu = new GLU();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        //gl.glLoadIdentity();

        if (height <= 0) { // avoid a divide by zero error!

            height = 1;
        }
        final float h = (float) width / (float) height;
        if (width < height)
            gl.glOrtho(-1.0, 1.0, -1.0 / h, 1.0 / h, -100.0, 100.0);
        else
            gl.glOrtho(-1.0 * h, 1.0 * h, -1.0, 1.0, -100.0, 100.0);
        //gl.glLoadIdentity();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL.GL_NICEST);
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glPointSize(1.5f);
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0f);
        // Clear the drawing area
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        // Reset the current matrix to the "identity"
        gl.glPushMatrix();
/*        gl.glLoadIdentity();*/
        // Move the "drawing cursor" around

        float[] matrix = new float[16];
        gl.glGetFloatv(gl.GL_MODELVIEW_MATRIX, FloatBuffer.wrap(matrix));
        gl.glLoadIdentity();
        gl.glRotated(rotate.z, rotate.x, rotate.y, 0.0);
        gl.glMultMatrixf(FloatBuffer.wrap(matrix));

        if (scene != null && !scene.getPolygons().isEmpty()) {
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glColor3d(1, 0, 0);
            gl.glLineWidth(1.5f);
            List<Polygon> polygons = scene.getPolygons();
            Bounds bounds = scene.getBounds();
            double sceneBound = Stream.of(bounds.getMax().x, bounds.getMax().y, bounds.getMax().z,
                    bounds.getMin().x, bounds.getMin().y, bounds.getMin().z).map(Math::abs).max(Double::compareTo).get() * 1.5;
            polygons.forEach(polygon -> polygon.vertices.forEach(vertex ->
                    gl.glVertex3d(vertex.pos.x / sceneBound, vertex.pos.y / sceneBound, vertex.pos.z / sceneBound)
            ));
            gl.glEnd();
        }

        if (points != null && !points.isEmpty()) {
            gl.glBegin(GL.GL_POINTS);
            gl.glColor3d(0,0,1);
            gl.glPointSize(1f);
            points.forEach(point -> gl.glVertex3d(point.x/ (bounds*1.5), point.y/(bounds*1.5), point.z/(bounds*1.5)));
            gl.glEnd();
        }
                gl.glBegin(GL.GL_LINES);
        gl.glColor3d(1, 0, 0);
        gl.glVertex2d(0, 0);
        gl.glVertex2d(1, 0);
        gl.glColor3d(0, 1, 0);
        gl.glVertex2d(0, 0);
        gl.glVertex2d(0, 1);
        gl.glEnd();

        // ���������� ��������� �������
        gl.glPopMatrix();
        //exViewer.viewAll(gl);
        // Flush all drawing operations to the graphics card
        gl.glFlush();

    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }
}

