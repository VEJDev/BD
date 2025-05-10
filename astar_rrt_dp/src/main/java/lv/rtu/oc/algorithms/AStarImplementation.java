package lv.rtu.oc.algorithms;

import lv.rtu.oc.Main;
import lv.rtu.oc.data.State;
import lv.rtu.oc.visualization.VisualizationBase;
import lv.rtu.oc.visualization.VisualizationPanelAStar;
import lv.rtu.oc.util.intervalarithmetic.IntervalDouble;
import lv.rtu.oc.visualization.VisualizationPanelReferenceLines;

import javax.swing.*;
import java.util.*;

public class AStarImplementation implements IAlgorithm {

    private static Double[][] inputs = null;
    private static final IntervalDouble inputInterval = new IntervalDouble(0, 1);
    private static final VisualizationBase visualizationPanel = new VisualizationPanelAStar();

    @Override
    public void init(Double[][] inputs) {
        AStarImplementation.inputs = inputs;

        JFrame frame = new JFrame("A* Live Visualization");
        frame.add(visualizationPanel);
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            List<Node> result = findOptimalControl();
            int i = 0;
            for (Node n : result.reversed()) {
                i++;
                if (i == 1) continue;
                System.out.println(n);
            }
            i = 0;
            for (Node n : result.reversed()) {
                i++;
                if (i == 1) continue;
                System.out.println(n.state.positionX.getMin() + ", " + n.state.positionZ.getMin());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Node implements Comparable<Node> {
        State state;
        Double[] input;
        int g;
        double h, f;
        double distanceToReferenceLine;

        Node parent;

        public Node(State state, Double[] input) {
            this.state = state;
            this.input = input;
            this.distanceToReferenceLine = Main.referencePath.closestDistanceToReferenceLine(state);
        }

        public int compareTo(Node other) {
            if (this.f != other.f) {
                return Double.compare(this.f, other.f);
            } else {
                if (this.h != other.h) {
                    return Double.compare(this.h, other.h);
                }
            }

            return Double.compare(this.h, other.h);
        }

        @Override
        public String toString() {
            return input[0] + "\t" + input[1] + "\t" + input[2] + "\t" + input[3];
        }
    }

    public static List<Node> findOptimalControl() throws Exception {
        Node startNode = new Node(Main.environment.initialState, new Double[]{0.0D, 0.0D, 0.0D, 0.0D});
        PriorityQueue<Node> openList = new PriorityQueue<>();
        Set<Node> closedList = new HashSet<>();
        startNode.h = Main.environment.findRequiredSteps(startNode.state, inputInterval, inputInterval, inputInterval, inputInterval);
        openList.add(startNode);
        long inputsChecked = 0;
        long timestamp = System.currentTimeMillis();

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            if (current.state.positionX.collides(Main.environment.goalRegion.positionX) && current.state.positionZ.collides(Main.environment.goalRegion.positionZ)) {
                List<Node> path = new ArrayList<>();
                while (current != null) {
                    path.add(current);
                    current = current.parent;
                }
                return path;
            }

            closedList.add(current);
            if (System.currentTimeMillis() - timestamp > 10) {
                timestamp = System.currentTimeMillis();
                Main.exploredStates.add(current.state);
                if(Main.exploredStates.size() > 1000) {
                    visualizationPanel.update();
                    System.out.println(current.g + ", " + current.h + ", " + openList.size() + ", " + inputsChecked + ", " + current.distanceToReferenceLine);
                    for (int i = 0; i<100 ; i++) {
                        Main.exploredStates.removeFirst();
                    }
                }
            }

            for (Double[] i : inputs) {
                inputsChecked++;
                State newState = current.state.clone().applyPhysics(new IntervalDouble(i[0], i[0]), new IntervalDouble(i[1], i[1]), new IntervalDouble(i[2], i[2]), new IntervalDouble(i[3], i[3]));
                if (Main.environment.isValid(newState) && !closedList.contains(new Node(newState, i))) {
                    Node neighbor = new Node(newState, i);

                    int gCost = current.g + 1;

                    double hCost = Main.environment.findRequiredSteps(neighbor.state, inputInterval, inputInterval, inputInterval, inputInterval);
                    double fCost = gCost + hCost;

                    if ((!openList.contains(neighbor) || fCost < neighbor.f) && Math.abs(hCost - current.h) < 5.0D && hCost <= current.h) {
                        neighbor.g = gCost;
                        neighbor.h = hCost;
                        neighbor.f = fCost;
                        neighbor.parent = current;

                        openList.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

}
