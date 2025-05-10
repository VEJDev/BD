package lv.rtu.oc.visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;

public abstract class VisualizationBase extends JPanel {

    protected int offsetX = 300;
    protected int offsetY = 300;
    protected double scaleFactor = 20.0;
    private boolean dragging = false;
    private int dragStartX;
    private int dragStartY;
    private long timeSinceLastUpdate;

    public VisualizationBase() {

        setLayout(new BorderLayout());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStartX = e.getX();
                dragStartY = e.getY();
                dragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    int dx = e.getX() - dragStartX;
                    int dy = e.getY() - dragStartY;

                    offsetX += dx;
                    offsetY += dy;

                    dragStartX = e.getX();
                    dragStartY = e.getY();

                    repaint();
                }
            }
        });

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if (notches < 0) {
                    scaleFactor = Math.min(200, (scaleFactor * 1.1));
                } else {
                    scaleFactor = Math.max(1, (scaleFactor * 0.9));
                }

                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    offsetX = 300;
                    offsetY = 300;
                }
            }
        });
    }

    public void update() {
        if (System.currentTimeMillis() - timeSinceLastUpdate > 50) {
            timeSinceLastUpdate = System.currentTimeMillis();
            repaint();
        }
    }

}
