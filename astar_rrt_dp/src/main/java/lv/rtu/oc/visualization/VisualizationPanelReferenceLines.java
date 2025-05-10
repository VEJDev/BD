package lv.rtu.oc.visualization;

import lv.rtu.oc.Main;
import lv.rtu.oc.data.ReferencePath;
import lv.rtu.oc.util.Line;

import java.awt.*;
import java.util.LinkedList;

public class VisualizationPanelReferenceLines extends VisualizationBase {

    private static final LinkedList<ReferencePath> referencePaths = new LinkedList<>();

    public VisualizationPanelReferenceLines() {
        super();
        //referencePaths.add(new ReferencePath("C:\\Users\\user\\Desktop\\paths\\ref_l\\env2\\astar.csv", new Color(255, 0, 0)));
        //referencePaths.add(new ReferencePath("C:\\Users\\user\\Desktop\\paths\\ref_l\\env2\\dp.csv", new Color(255, 128, 0)));
        //referencePaths.add(new ReferencePath("C:\\Users\\user\\Desktop\\paths\\ref_l\\env2\\rl.csv", new Color(0, 0, 255)));
        referencePaths.add(new ReferencePath("C:\\Users\\user\\Desktop\\paths\\ref_l\\env3\\rrt1.csv", new Color(0, 255, 0)));
        referencePaths.add(new ReferencePath("C:\\Users\\user\\Desktop\\paths\\ref_l\\env3\\rrt2.csv", new Color(0, 225, 0)));
        referencePaths.add(new ReferencePath("C:\\Users\\user\\Desktop\\paths\\ref_l\\env3\\rrt3.csv", new Color(0, 195, 0)));
        referencePaths.add(new ReferencePath("C:\\Users\\user\\Desktop\\paths\\ref_l\\env3\\rrt4.csv", new Color(0, 165, 0)));
        referencePaths.add(new ReferencePath("C:\\Users\\user\\Desktop\\paths\\ref_l\\env3\\rrt5.csv", new Color(0, 135, 0)));
        referencePaths.add(new ReferencePath("C:\\Users\\user\\Desktop\\paths\\ref_l\\env3\\human.csv", new Color(255, 0, 0)));
    }


    @Override
    protected void paintComponent(Graphics g) {
        try {
            super.paintComponent(g);

            Main.environment.drawEnvironment(g, offsetX, offsetY, scaleFactor);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(3));

            for (ReferencePath refl : referencePaths) {
                g2d.setColor(refl.color);
                for (Line line : refl.referencePath) {
                    g2d.drawLine((int) ((-line.x1) * scaleFactor + offsetX), (int) (line.z1 * scaleFactor + offsetY),
                            (int) ((-line.x2) * scaleFactor + offsetX), (int) (line.z2 * scaleFactor + offsetY));
                }
            }

        } catch (Exception _) {

        }
    }

}