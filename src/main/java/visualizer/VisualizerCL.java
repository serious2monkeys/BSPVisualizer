package visualizer;

import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import visualizer.engine.CSG;
import visualizer.engine.STL;
import visualizer.engine.Vector3d;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static visualizer.VisualizerCL.VisualizerMode.POLYGONAL;
import static visualizer.VisualizerCL.VisualizerMode.REYCAST_JAVA;
/*
 * Created by JFormDesigner on Sun Nov 27 18:32:31 YEKT 2016
 */


/**
 * @author unknown
 */
public class VisualizerCL extends JFrame {

    private FPSAnimator animator;
    private GLRenderer renderer;
    private CSG scene;
    private VisualizerMode visualizerMode = POLYGONAL;
    private int branchSize = 4;

    private boolean mousePressed = false;
    private Vector2d prevMousePos = new Vector2d(0.0, 0.0);
    private Vector3d rotVector = new Vector3d(0, 0, 0);

    MouseAdapter adapter = new MouseInputAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            mousePressed = true;
            prevMousePos = new Vector2d(e.getX(), e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // mousePressed = false;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (mousePressed) {
                int dx = (int) (prevMousePos.x - e.getX());
                int dy = (int) (prevMousePos.y - e.getY());
                rotVector = new Vector3d(dy, dx, 0);
                rotVector.z = Math.sqrt(dx * dx + dy * dy) / 3;

                panel2.removeGLEventListener(renderer);
                renderer = visualizerMode.equals(POLYGONAL)
                        ? new GLRenderer(scene)
                        : new GLRenderer();
                renderer.setRotate(rotVector);
                renderer.init(panel2);
                panel2.addGLEventListener(renderer);
                panel2.setAnimator(animator);
                animator.start();

                prevMousePos = new Vector2d(e.getX(), e.getY());
                mousePressed = false;
            }
        }
    };

    enum VisualizerMode {
        POLYGONAL,
        REYCAST_JAVA
    }

  /*
    // Generated using JFormDesigner Evaluation license - Anton Doronin
    private JPanel panel1;
    private JButton button1;
    private GLJPanel panel2;
*/

    public VisualizerCL() {
        initComponents();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread(() -> {
                    animator.stop();
                    System.exit(0);
                }).start();
            }
        });
    }

/*    private void initComponents() {
        panel1 = new JPanel();
        button1 = new JButton();
        panel2 = new GLJPanel();
        animator = new FPSAnimator(panel2, 60, true);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.LINE_AXIS));

        //======== panel1 ========
        {

            // JFormDesigner evaluation mark
            panel1.setBorder(new javax.swing.border.CompoundBorder(
                    new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
                            "JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
                            javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
                            java.awt.Color.red), panel1.getBorder()));
            panel1.addPropertyChangeListener(e -> {
                if ("border".equals(e.getPropertyName())) throw new RuntimeException();
            });

            panel1.setLayout(null);

            //---- button1 ----
            button1.setText("text");
            panel1.add(button1);
            button1.setBounds(5, 10, 95, button1.getPreferredSize().height);
            panel1.add(panel2);
            panel2.setBounds(110, 10, 590, 270);

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for (int i = 0; i < panel1.getComponentCount(); i++) {
                    Rectangle bounds = panel1.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = panel1.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                panel1.setMinimumSize(preferredSize);
                panel1.setPreferredSize(preferredSize);
            }
        }
        contentPane.add(panel1);
        pack();
        setLocationRelativeTo(getOwner());
    }*/

    private void menuItem1ActionPerformed(ActionEvent e) throws IOException {
        JFileChooser fileChooser = new JFileChooser("/home/terravitus/git/projects/BSPOpenCLVisualizer" +
                "/src/main/resources");
        fileChooser.setVisible(true);
        fileChooser.showDialog(panel2, "Загрузить");
        fileChooser.setVisible(false);
        if (fileChooser.getSelectedFile() != null) {
            scene = STL.file(fileChooser.getSelectedFile().toPath());
            scene.setOptType(CSG.OptType.POLYGON_BOUND);
            panel2.removeGLEventListener(renderer);
            List<Vector3d[][]> rays = splitRays(scene);
            renderer = visualizerMode.equals(POLYGONAL)
                    ? new GLRenderer(scene)
                    : new GLRenderer();
            renderer.init(panel2);
            panel2.addGLEventListener(renderer);
            panel2.setAnimator(animator);
            animator.start();
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Anton Doronin
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        menuItem1 = new JMenuItem();
        menu2 = new JMenu();
        menuItem2 = new JMenuItem();
        menuItem3 = new JMenuItem();
        panel1 = new JPanel();
        panel2 = new GLJPanel();
        renderer = new GLRenderer();
        animator = new FPSAnimator(panel2, 60, true);
        panel2.setForeground(Color.lightGray);
        panel2.addGLEventListener(renderer);
        panel2.setAnimator(animator);
        animator.start();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.LINE_AXIS));

        {
            panel2.addMouseListener(adapter);
            panel2.addMouseMotionListener(adapter);
        }
        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u043c\u043e\u0434\u0435\u043b\u0435\u0439");

                //---- menuItem1 ----
                menuItem1.setText("\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c");
                menuItem1.addActionListener((e) -> {
                    try {
                        menuItem1ActionPerformed(e);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
                menu1.add(menuItem1);
            }
            menuBar1.add(menu1);

            //======== menu2 ========
            {
                menu2.setText("\u0421\u043f\u043e\u0441\u043e\u0431\u044b \u0432\u0438\u0437\u0443\u0430\u043b\u0438\u0437\u0430\u0446\u0438\u0438");

                //---- menuItem2 ----
                menuItem2.setText("\u0413\u0435\u043d\u0435\u0440\u0430\u0446\u0438\u044f \u043f\u043e\u0432\u0435\u0440\u0445\u043d\u043e\u0441\u0442\u0438");
                menuItem2.addActionListener(e -> visualizerMode = POLYGONAL);
                menu2.add(menuItem2);

                //---- menuItem3 ----
                menuItem3.setText("\u041f\u0430\u0440\u0430\u043b\u043b\u0435\u043b\u044c\u043d\u044b\u0439 \u0440\u0435\u0439\u043a\u0430\u0441\u0442\u0438\u043d\u0433");
                menuItem3.addActionListener(e -> visualizerMode = REYCAST_JAVA);
                menu2.add(menuItem3);
            }
            menuBar1.add(menu2);
        }
        setJMenuBar(menuBar1);

        //======== panel1 ========
        {

            // JFormDesigner evaluation mark
            panel1.addPropertyChangeListener(e -> {
                if ("border".equals(e.getPropertyName())) throw new RuntimeException();
            });

            panel1.setLayout(null);
            panel1.add(panel2);
            panel2.setBounds(0, 0, 500, 500);

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for (int i = 0; i < panel1.getComponentCount(); i++) {
                    Rectangle bounds = panel1.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = panel1.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                panel1.setMinimumSize(preferredSize);
                panel1.setPreferredSize(preferredSize);
            }
        }
        contentPane.add(panel1);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private List<Vector3d[][]> splitRays(CSG geometry) {
        List<Vector3d[][]> rays = new ArrayList<>();
        int pixelNum = panel2.getWidth();
        branchSize = (int) Math.sqrt(pixelNum);
        Vector3d bound = geometry.getBounds().getMax();
        double maxDimension = Stream.of(bound.x, bound.y, bound.z)
                .max(Double::compareTo).get();
        double step = (maxDimension / pixelNum) * 2;
        for (int pos = 0; pos <= pixelNum; pos += branchSize) {
            Vector3d[][] coherentRays = new Vector3d[branchSize][branchSize];
            double delta = -maxDimension + pos * step;
            for (int i = 0; i < branchSize; i++) {
                for (int j = 0; j < branchSize; j++) {
                    coherentRays[i][j] = new Vector3d(delta + i * step, delta + j * step, Double.POSITIVE_INFINITY);
                }
            }
            rays.add(coherentRays);
        }
        return rays;
    }

    @Override
    public void setVisible(boolean show) {
        if (!show) {
            if (animator != null) {
                animator.stop();
            }
        }
        super.setVisible(show);
        if (!show)
            if (animator != null) {
                animator.start();
            }
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Anton Doronin
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem menuItem1;
    private JMenu menu2;
    private JMenuItem menuItem2;
    private JMenuItem menuItem3;
    private JPanel panel1;
    private GLJPanel panel2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
