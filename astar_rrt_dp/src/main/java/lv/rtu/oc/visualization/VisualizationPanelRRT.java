package lv.rtu.oc.visualization;

import lv.rtu.oc.Main;
import lv.rtu.oc.algorithms.RRTImplementation;
import lv.rtu.oc.data.State;

import javax.swing.*;
import java.awt.*;

public class VisualizationPanelRRT extends VisualizationBase {

    private final JSlider zoomSlider;
    private final JSlider secondarySlider;

    public VisualizationPanelRRT() {
        super();

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridLayout(1, 2));

        zoomSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 2);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
        zoomSlider.addChangeListener(e -> {
            RRTImplementation.randomness = zoomSlider.getValue();
            repaint();
        });

        secondarySlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 40);
        secondarySlider.setPaintTicks(true);
        secondarySlider.setPaintLabels(true);
        secondarySlider.addChangeListener(e -> {
            RRTImplementation.maxLookaheadDistance = secondarySlider.getValue();
            repaint();
        });

        sliderPanel.add(zoomSlider);
        sliderPanel.add(secondarySlider);
        add(sliderPanel, BorderLayout.SOUTH);

    }

    @Override
    protected void paintComponent(Graphics g) {
        try {
            super.paintComponent(g);

            Main.environment.drawEnvironment(g, offsetX, offsetY, scaleFactor);

            // Reference Line
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(Color.YELLOW);
            /*for (Line line : Main.referencePath.referencePath) {
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

            g.setColor(new Color(0, 255, 0));
            g2d.setStroke(new BasicStroke(2));
            for (RRTImplementation.Node node : RRTImplementation.tree) {
                if (node.parent != null) {
                    int x1 = (int) ((-node.state.positionX.getMin()) * scaleFactor + offsetX);
                    int z1 = (int) (node.state.positionZ.getMin() * scaleFactor + offsetY);
                    int x2 = (int) ((-node.parent.state.positionX.getMin()) * scaleFactor + offsetX);
                    int z2 = (int) (node.parent.state.positionZ.getMin() * scaleFactor + offsetY);

                    g2d.drawLine(x1, z1, x2, z2);
                }
            }

            RRTImplementation.Node currentNode = RRTImplementation.bestNode;
            if (currentNode != null) {
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(3));
                while (currentNode != null) {
                    if (currentNode.parent != null) {
                        int x1 = (int) ((-currentNode.state.positionX.getMin()) * scaleFactor + offsetX);
                        int z1 = (int) (currentNode.state.positionZ.getMin() * scaleFactor + offsetY);
                        int x2 = (int) ((-currentNode.parent.state.positionX.getMin()) * scaleFactor + offsetX);
                        int z2 = (int) (currentNode.parent.state.positionZ.getMin() * scaleFactor + offsetY);

                        g2d.drawLine(x1, z1, x2, z2);
                    }
                    currentNode = currentNode.parent;
                }
            }
        } catch (Exception e) {
            ;
        }

    }

}