package visualizer;

import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.STL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
/*
 * Created by JFormDesigner on Sun Nov 27 18:32:31 YEKT 2016
 */


/**
 * @author unknown
 */
public class VisualizerCL extends JFrame {

    private FPSAnimator animator;
    private GLRenderer renderer;
    CSG scene;
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
                // Run this on another thread than the AWT event queue to
                // make sure the call to Animator.stop() completes before
                // exiting
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

        animator = new FPSAnimator(panel2, 60, true);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.LINE_AXIS));

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
                menu2.add(menuItem2);

                //---- menuItem3 ----
                menuItem3.setText("\u041f\u0430\u0440\u0430\u043b\u043b\u0435\u043b\u044c\u043d\u044b\u0439 \u0440\u0435\u0439\u043a\u0430\u0441\u0442\u0438\u043d\u0433");
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
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
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
