package visualizer;

import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import visualizer.benchmark.TreeSampler;
import visualizer.engine.*;
import visualizer.engine.solid.LineSegment;
import visualizer.engine.solid.SolidTreeNode;
import visualizer.engine.transform.TreeConvertedInstance;
import visualizer.openCL.CLInterface;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static visualizer.VisualizerCL.VisualizerMode.POLYGONAL;
import static visualizer.VisualizerCL.VisualizerMode.REYCAST_JAVA;

/**
 * @author unknown
 */
public class VisualizerCL extends JFrame {

    private FPSAnimator animator;
    private GLRenderer renderer;
    private CSG scene;
    private double[] begins, ends;
    private SolidTreeNode sceneTree;
    private List<LineSegment> segments;
    private VisualizerMode visualizerMode = POLYGONAL;
    private int branchSize = 4;
    double maxDimension = 1.0;
    List<Vector3d> result = new ArrayList<>();

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
            //mousePressed = false;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (mousePressed) {
                int dx = (int) (e.getX() - prevMousePos.x);
                int dy = (int) (e.getY() - prevMousePos.y) ;
                rotVector = new Vector3d(dy, dx, 0);
                rotVector.z = Math.sqrt(dx * dx + dy * dy) / 3;

                panel2.removeGLEventListener(renderer);
                switch (visualizerMode) {
                    case POLYGONAL:
                        renderer = new GLRenderer(scene);
                        break;
                    case REYCAST_JAVA:
                        renderer = new GLRenderer(maxDimension, result);
                }
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

    private void menuItem1ActionPerformed(ActionEvent e) throws IOException, InterruptedException {
        JFileChooser fileChooser = new JFileChooser("/home/terravitus/git/projects/BSPOpenCLVisualizer" +
                "/src/main/resources");
        scene = null;
        fileChooser.setVisible(true);
        fileChooser.showDialog(panel2, "Загрузить");
        fileChooser.setVisible(false);
        if (fileChooser.getSelectedFile() != null) {
            scene = STL.file(fileChooser.getSelectedFile().toPath());
            scene.setOptType(CSG.OptType.POLYGON_BOUND);
            panel2.removeGLEventListener(renderer);
            switch (visualizerMode) {
                case POLYGONAL:
                    renderer = new GLRenderer(scene);
                    break;
                case REYCAST_JAVA:
                    Long before = System.nanoTime();
                    sceneTree = new SolidTreeNode(scene.getPolygons());
                    System.out.println("TREE BUILDING " + (System.nanoTime() - before)/1000000);
                    before = System.nanoTime();
                    TreeConvertedInstance instance = new TreeConvertedInstance(sceneTree);
                    System.out.println("Converting " + (System.nanoTime() - before)/1000000);
                    segments = makeSimpleSplit(scene);

                    before = System.nanoTime();
                    List<Vector3d> points = segments.stream().map(sceneTree::checkIntersection)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    System.out.println("INTERSECTING OBJECT " + (System.nanoTime() - before)/1000000);
                    before = System.nanoTime();
                    List<Vector3d> alternative = segments.stream().map(instance::checkIntersection)
                            .filter(Objects::nonNull).collect(Collectors.toList());
                    System.out.println("INTERSECTING ARRAYS " + (System.nanoTime() - before)/1000000);

                    begins = segments.stream().map(segment -> {
                        List<Double> coords = new ArrayList<>();
                        Collections.addAll(coords, new Double[]{segment.getBegin().x, segment.getBegin().y, segment.getBegin().z});
                        return coords;
                    }).reduce(new ArrayList<>(), (doubles, doubles2) -> {
                        doubles.addAll(doubles2);
                        return doubles;
                    }).stream().mapToDouble(Double::doubleValue).toArray();
                    ends = segments.stream().map(segment -> {
                        List<Double> coords = new ArrayList<>();
                        Collections.addAll(coords, new Double[]{segment.getEnd().x, segment.getEnd().y, segment.getEnd().z});
                        return coords;
                    }).reduce(new ArrayList<>(), (doubles, doubles2) -> {
                        doubles.addAll(doubles2);
                        return doubles;
                    }).stream().mapToDouble(Double::doubleValue).toArray();
//-------------------------------------------------------------
                    System.out.println("Count of nodes " + instance.countCells());
                    CLInterface wizard = new CLInterface(instance);
                    List<Vector3d> parallelMagic = wizard.checkIntersection(begins, ends);
                    before = System.nanoTime();
                    List<Vector3d> arraySegments = new ArrayList<>();
                    for(int i =0; i<begins.length; i+=3) {
                        double[] begin = new double[] {begins[i], begins[i+1], begins[i+2]};
                        double[] end = new double[] {ends[i], ends[i+1], ends[i+2]};
                        double[] intersection = instance.checkIntersection(begin, end);
                        if (intersection != null) {
                            arraySegments.add(new Vector3d(intersection[0], intersection[1], intersection[2]));
                        }
                    }
                    System.out.println("ARRAYS PROCESSING " + (System.nanoTime() - before)/1000000);


                    Bounds bounds = scene.getBounds();
                    maxDimension = Stream.of(bounds.getMax().x, bounds.getMax().y, bounds.getMax().z,
                            bounds.getMin().x, bounds.getMin().y, bounds.getMin().z)
                            .map(Math::abs)
                            .max(Double::compareTo).get();
                    result = parallelMagic;
                    renderer = new GLRenderer(maxDimension, parallelMagic);

            }
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
        menuItem4 = new JMenuItem();
        menuItem5 = new JMenuItem();
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
                    } catch (InterruptedException e1) {
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

                menuItem4.setText("Test Coherence");
                menuItem4.addActionListener(e -> {
                    try {
                        testCoherenceSelected();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });
                menu2.add(menuItem4);

                menuItem5.setText("Test balanced");
                menuItem5.addActionListener(e -> {
                    try {
                        testBalanced();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });

                menu2.add(menuItem5);
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
                    preferredSize.width = Math.max((bounds.x + bounds.width), preferredSize.width);
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

    private void testBalanced() throws IOException, InterruptedException {
        sceneTree = TreeSampler.getBalancedTree();
        TreeConvertedInstance instance = new TreeConvertedInstance(sceneTree);
        scene = new Cube(2).toCSG();
        segments = makeSimpleSplit(scene);
        begins = segments.stream().map(segment -> {
            List<Double> coords = new ArrayList<>();
            Collections.addAll(coords, new Double[]{segment.getBegin().x, segment.getBegin().y, segment.getBegin().z});
            return coords;
        }).reduce(new ArrayList<>(), (doubles, doubles2) -> {
            doubles.addAll(doubles2);
            return doubles;
        }).stream().mapToDouble(Double::doubleValue).toArray();
        ends = segments.stream().map(segment -> {
            List<Double> coords = new ArrayList<>();
            Collections.addAll(coords, new Double[]{segment.getEnd().x, segment.getEnd().y, segment.getEnd().z});
            return coords;
        }).reduce(new ArrayList<>(), (doubles, doubles2) -> {
            doubles.addAll(doubles2);
            return doubles;
        }).stream().mapToDouble(Double::doubleValue).toArray();
//-------------------------------------------------------------
        System.out.println("Count of nodes " + instance.countCells());
        CLInterface wizard = new CLInterface(instance);
        List<Vector3d> parallelMagic = wizard.checkIntersection(begins, ends);
        Bounds bounds = scene.getBounds();
        maxDimension = Stream.of(bounds.getMax().x, bounds.getMax().y, bounds.getMax().z,
                bounds.getMin().x, bounds.getMin().y, bounds.getMin().z)
                .map(Math::abs)
                .max(Double::compareTo).get();
        result = parallelMagic;
        renderer = new GLRenderer(maxDimension, parallelMagic);
        renderer.init(panel2);
        panel2.addGLEventListener(renderer);
        panel2.setAnimator(animator);
        animator.start();


        /***/
        Long before = System.nanoTime();
        List<Vector3d> alternative = segments.parallelStream().map(instance::checkIntersection)
                .filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println("INTERSECTING ARRAYS " + (System.nanoTime() - before)/1000000);
    }

    private void testCoherenceSelected() throws IOException, InterruptedException {
        sceneTree = TreeSampler.getCoherentTree();
        TreeConvertedInstance instance = new TreeConvertedInstance(sceneTree);
        scene = new Cube(2048.0).toCSG();
        segments = makeSimpleSplit(scene);
        begins = segments.stream().map(segment -> {
            List<Double> coords = new ArrayList<>();
            Collections.addAll(coords, new Double[]{segment.getBegin().x, segment.getBegin().y, segment.getBegin().z});
            return coords;
        }).reduce(new ArrayList<>(), (doubles, doubles2) -> {
            doubles.addAll(doubles2);
            return doubles;
        }).stream().mapToDouble(Double::doubleValue).toArray();
        ends = segments.stream().map(segment -> {
            List<Double> coords = new ArrayList<>();
            Collections.addAll(coords, new Double[]{segment.getEnd().x, segment.getEnd().y, segment.getEnd().z});
            return coords;
        }).reduce(new ArrayList<>(), (doubles, doubles2) -> {
            doubles.addAll(doubles2);
            return doubles;
        }).stream().mapToDouble(Double::doubleValue).toArray();
//-------------------------------------------------------------
        System.out.println("Count of nodes " + instance.countCells());
        CLInterface wizard = new CLInterface(instance);
        List<Vector3d> parallelMagic = wizard.checkIntersection(begins, ends);
        Bounds bounds = scene.getBounds();
        maxDimension = Stream.of(bounds.getMax().x, bounds.getMax().y, bounds.getMax().z,
                bounds.getMin().x, bounds.getMin().y, bounds.getMin().z)
                .map(Math::abs)
                .max(Double::compareTo).get();
        result = parallelMagic;
        renderer = new GLRenderer(maxDimension, parallelMagic);
        renderer.init(panel2);
        panel2.addGLEventListener(renderer);
        panel2.setAnimator(animator);
        animator.start();


        /***/
        Long before = System.nanoTime();
        List<Vector3d> alternative = segments.parallelStream().map(instance::checkIntersection)
                .filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println("INTERSECTING ARRAYS " + (System.nanoTime() - before)/1000000);
    }

    private List<LineSegment[][]> splitRays(CSG geometry) {
        List<LineSegment[][]> rays = new ArrayList<>();
        int pixelNum = panel2.getWidth();
        branchSize = (int) Math.sqrt(pixelNum);
        Vector3d bound = geometry.getBounds().getMax();
        double maxDimension = Stream.of(bound.x, bound.y, bound.z)
                .max(Double::compareTo).get();
        double step = (maxDimension / pixelNum) * 2;
        for (int pos = 0; pos <= pixelNum; pos += branchSize) {
            LineSegment[][] coherentRays = new LineSegment[branchSize][branchSize];
            double delta = -maxDimension + pos * step;
            for (int i = 0; i < branchSize; i++) {
                for (int j = 0; j < branchSize; j++) {
                    Vector3d begin = new Vector3d(delta + i * step, delta + j * step, maxDimension);
                    Vector3d end = begin.clone();
                    end.z = maxDimension;
                    coherentRays[i][j] = new LineSegment(begin, end);
                }
            }
            rays.add(coherentRays);
        }
        return rays;
    }

    private List<LineSegment> makeSimpleSplit(CSG geometry) {
        List<LineSegment> rays = new ArrayList<>();
        int height = panel2.getHeight();
        int width = panel2.getWidth();
        Bounds bounds = geometry.getBounds();
        double maxDimension = Stream.of(bounds.getMax().x, bounds.getMax().y, bounds.getMax().z,
                bounds.getMin().x, bounds.getMin().y, bounds.getMin().z)
                .map(Math::abs)
                .max(Double::compareTo).get();
        int box = width > height ? height : width;
        double step = (maxDimension / (box)) * 2;
        for (int pos = 0; pos < box; pos++) {
            double yCord = -maxDimension + pos * step;
            for (int i = 0; i < box; i++) {
                Vector3d begin = new Vector3d(-maxDimension + i * step, yCord, maxDimension);
                Vector3d end = begin.clone();
                end.z = -maxDimension;
                rays.add(new LineSegment(begin, end));
            }
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
    private JMenuItem menuItem4;
    private JMenuItem menuItem5;
    private JPanel panel1;
    private GLJPanel panel2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
