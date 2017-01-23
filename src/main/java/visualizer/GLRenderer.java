package visualizer;


import com.jogamp.opengl.*;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vector3d;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.stream.Stream;

public class GLRenderer implements GLEventListener {

    private CSG scene;

    private Vector3d rotate = new Vector3d(0, 0, 0);

    public GLRenderer() {
        scene = null;
    }

    public GLRenderer(CSG scene) {
        this.scene = scene;
    }

    public void setRotate(Vector3d rotate) {
        this.rotate = rotate;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        // Enable VSync
        //gl.setSwapInterval(1);
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0f);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glClearDepth(1.0f);
        gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, FloatBuffer.wrap(new float[]{1f, 1f, 1f, 0}));
        gl.glMaterialfv(gl.GL_FRONT, gl.GL_SPECULAR, FloatBuffer.wrap(new float[]{1.0f, 1.0f, 1.0f, 1.0f}));
        gl.glMaterialfv(gl.GL_FRONT, gl.GL_SHININESS, FloatBuffer.wrap(new float[]{50f}));
        gl.glEnable(gl.GL_LIGHTING);
        gl.glEnable(gl.GL_LIGHT0);
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
            Vector3d maxBounds = scene.getBounds().getBounds();
            double bound = Stream.of(maxBounds.x, maxBounds.y, maxBounds.z).max(Double::compareTo).get();
            polygons.forEach(polygon -> polygon.vertices.forEach(vertex ->
                    gl.glVertex3d(vertex.pos.x / bound, vertex.pos.y / bound, vertex.pos.z / bound)
            ));
            gl.glEnd();

            gl.glColor3d(0, 0, 0);
            gl.glBegin(GL.GL_POINTS);
            polygons.forEach(polygon -> polygon.vertices.forEach(vertex ->
                    gl.glVertex3d(vertex.pos.x / bound, vertex.pos.y / bound, vertex.pos.z / bound)
            ));
            gl.glEnd();
        }
        /*
         gl.glEnable(GL.GL_LINE_SMOOTH);
            gl.glShadeModel(GL.GL_SMOOTH);
            gl.glLineWidth(1.5f);
            gl.glBegin(GL.GL_LINES);
            gl.glColor3d(1,0,0);
            gl.glVertex3d(0,0,0);
            gl.glVertex3d(5, 0, 0);
            gl.glColor3d(0, 5, 0);
            gl.glVertex3d(0, 0, 0);
            gl.glVertex3d(0, 5, 0);
            gl.glColor3d(0, 0, 5);
            gl.glVertex3d(0, 0, 0);
            gl.glVertex3d(0, 0, 5);
            gl.glEnd();
        gl.glPushMatrix();*/
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

