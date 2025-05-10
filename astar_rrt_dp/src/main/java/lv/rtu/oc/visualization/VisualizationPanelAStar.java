package lv.rtu.oc.visualization;

import lv.rtu.oc.Main;
import lv.rtu.oc.data.State;

import java.awt.*;

public class VisualizationPanelAStar extends VisualizationBase {

    @Override
    protected void paintComponent(Graphics g) {
        try {
            super.paintComponent(g);

            Main.environment.drawEnvironment(g, offsetX, offsetY, scaleFactor);

            // Reference Line
            /*Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(Color.YELLOW);
            for (Line line : Main.referencePath.referencePath) {
                g2d.drawLine((int) ((-line.x1) * scaleFactor + offsetX), (int) (line.z1 * scaleFactor + offsetY),
                        (int) ((-line.x2) * scaleFactor + offsetX), (int) (line.z2 * scaleFactor + offsetY));
            }*/

            int i = 0;
            for (State state : Main.exploredStates) {
                int x = (int) ((-state.positionX.getMin()) * scaleFactor + offsetX);
                int z = (int) (state.positionZ.getMin() * scaleFactor + offsetY);
                i++;
                g.setColor(new Color(i / 4, 0, 0));
                g.fillOval(x, z, 5, 5);
            }
        } catch (Exception _) {

        }
    }

}