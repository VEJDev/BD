package lv.rtu.oc.algorithms;

import lv.rtu.oc.Main;
import lv.rtu.oc.data.Environment3;
import lv.rtu.oc.data.State;
import lv.rtu.oc.util.LocationEncoder;
import lv.rtu.oc.visualization.VisualizationPanelRRT;
import lv.rtu.oc.util.intervalarithmetic.IntervalDouble;

import javax.swing.*;
import java.util.*;

public class RRTImplementation implements IAlgorithm {

    private static Double[][] inputs = null;
    private static final IntervalDouble inputInterval = new IntervalDouble(0, 1);
    private static final VisualizationPanelRRT visualizationPanel = new VisualizationPanelRRT();
    public static final List<Node> tree = new ArrayList<>();
    static long timestamp;
    public static Node bestNode;
    public static double maxLookaheadDistance = 40;
    public static double distanceToLine = 50.0;
    public static int clearTimeout = 1000;
    static Random rand = new Random();
    public static int randomness = 600000;
    public static double shortestDistanceToGoal;

    @Override
    public void init(Double[][] inputs) {
        RRTImplementation.inputs = inputs;

        JFrame frame = new JFrame("RRT* Live Visualization");
        frame.add(visualizationPanel);
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        shortestDistanceToGoal = Main.environment.approximateDistanceToFinish(Main.environment.initialState.positionX.getMin(), Main.environment.initialState.positionZ.getMin());

        long start = System.currentTimeMillis();
        try {
            Node n = rrt(Main.environment.initialState);
            LinkedList<Node> ll = new LinkedList<>();
            while (n.parent != null) {
                ll.add(n);
                n = n.parent;
            }
            int i = 0;
            System.out.println(ll.size() + " steps");
            for (RRTImplementation.Node n2 : ll.reversed()) {
                i++;
                System.out.println(n2);
            }
            for (RRTImplementation.Node n2 : ll.reversed()) {
                i++;
                System.out.println(n2.state.positionX.getMin() + ", " + n2.state.positionZ.getMin());
            }

        } catch (Exception e) {
            ;
        }
    }

    public static class Node implements Comparable<Node> {
        public State state;
        public Node parent;
        public double stepsToFinish;
        double cost;
        Double[] input;
        int step;
        double distanceToFinish;

        public Node(State state, Node parent, double cost, Double[] input) {
            this.state = state;
            this.parent = parent;
            this.distanceToFinish = Main.environment.approximateDistanceToFinish(state.positionX.getMin(), state.positionZ.getMin());
            if (parent != null) {
                this.step = parent.step + 1;
            }
            this.cost = this.stepsToFinish = cost;
            this.cost = getRealCost();
            this.input = input;
            Node a = this;
            while (a.parent != null) {
                a.cost = Math.min(this.cost, a.cost);
                a = a.parent;
            }
        }

        @Override
        public String toString() {
            return input[0] + "\t" + input[1] + "\t" + input[2] + "\t" + input[3];
        }

        public double getRealCost() {
            return 0.2 * cost + 0.1 * step + 0.7 * distanceToFinish;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(getRealCost(), o.getRealCost());
        }
    }

    public static Node rrt(State initialState) throws Exception {
        tree.add(new Node(initialState, null, 0, inputs[0]));

        for (int i = 0; i < 500000000; i++) {
            //Thread.sleep(1000);
            long randomKey = 0;
            double[] coords;
            if (Main.environment instanceof Environment3) {
                Iterator<Long> iterator = ((Environment3)Main.environment).environment.keySet().iterator();
                int randomIndex = rand.nextInt(((Environment3)Main.environment).environment.keySet().size());
                for (int j = 0; j <= randomIndex; j++) {
                    randomKey = iterator.next();
                }
                if (Math.abs(((Environment3)Main.environment).environment.get(randomKey) - shortestDistanceToGoal) > maxLookaheadDistance) continue;
                int[] ap = LocationEncoder.decode(randomKey);
                coords = new double[]{ap[0] + Math.random() - 1, ap[1] + Math.random() - 1};
            } else {
                coords = new double[]{Math.random() * 50 - 25, Math.random() * 50 - 25};
            }

            if (bestNode != null) {
                if (Main.referencePath.closestDistanceToReferenceLine(new State(new IntervalDouble(coords[0]), new IntervalDouble(coords[1]), new IntervalDouble(0.0),new IntervalDouble(0.0),new IntervalDouble(0.0),new IntervalDouble(0.0))) > distanceToLine) continue;
            }
            State sample = new State(new IntervalDouble(coords[0]), new IntervalDouble(coords[1]), new IntervalDouble(0), new IntervalDouble(0), new IntervalDouble(0), new IntervalDouble(0));
            Node nearest = getNearest(sample);
            Main.exploredStates.add(sample);
            if (Main.exploredStates.size() > 1000) {
                for (int h = 0; h < 100; h++) {
                    Main.exploredStates.removeFirst();
                }
            }
            visualizationPanel.update();
            if (System.currentTimeMillis() - timestamp > clearTimeout) {
                timestamp = System.currentTimeMillis();

                tree.sort(Node::compareTo);
                System.out.println(i + ", " + tree.size() + ", " + System.currentTimeMillis() + ", " + tree.getLast().cost + ", " + tree.getLast().getRealCost());
                while (tree.size() > 1500) {
                    Node n = tree.getLast();
                    double cost = n.cost;
                    tree.removeIf(node -> node.cost == cost);
                }
            }

            Node bestNewNode = null;
            double minCost = Double.MAX_VALUE;
            for (Double[] input : inputs) {
                State newState = nearest.state.clone().applyPhysics(new IntervalDouble(input[0], input[0]), new IntervalDouble(input[1], input[1]), new IntervalDouble(input[2], input[2]), new IntervalDouble(input[3], input[3]));
                if (!Main.environment.isValid(newState)) continue;
                double a = Main.environment.findRequiredSteps(newState, inputInterval, inputInterval, inputInterval, inputInterval);
                if (a == Integer.MAX_VALUE) continue;
                if (a + 5 < nearest.stepsToFinish) continue;
                double newCost = findRequiredStepsToNode(newState, sample);
                if (newCost < minCost) {
                    bestNewNode = new Node(newState, nearest, a, input);
                    double distToFinish = Main.environment.approximateDistanceToFinish(newState.positionX.getMin(), newState.positionZ.getMin());
                    if (shortestDistanceToGoal > distToFinish) {
                        shortestDistanceToGoal = distToFinish;
                    }
                    minCost = newCost;
                    bestNode = bestNewNode;
                }
            }

            if (bestNewNode != null) {
                tree.add(bestNewNode);
                if (bestNewNode.state.positionX.collides(Main.environment.goalRegion.positionX) && bestNewNode.state.positionZ.collides(Main.environment.goalRegion.positionZ)) {
                    return bestNewNode;
                }
            }
        }
        return null;
    }


    public static Node getNearest(State sample) throws Exception {
        Node nearest = null;
        double minDist = Double.MAX_VALUE;
        if (rand.nextInt(randomness) == 0) {
            int index = (int) (Math.random() * tree.size());
            return tree.get(index);
        }
        for (Node node : tree) {
            double dist = findRequiredStepsToNode(node.state, sample);
            if (dist < minDist) {
                minDist = dist;
                nearest = node;
            }
        }
        return nearest;
    }

    private static double findRequiredStepsToNode(State fromState, State toState) throws Exception {
        int steps = 0;
        State state = fromState.clone();
        while (steps < 1500) {
            if (state.positionX.contains(toState.positionX.getMin()) && state.positionZ.contains((toState.positionZ.getMin()))) {
                return steps;
            }
            state.applyPhysics(inputInterval, inputInterval, inputInterval, inputInterval);
            steps++;
        }
        throw new Exception("Nothing found");
    }

    private static double calculateClosestDistance(IntervalDouble positionX, IntervalDouble positionZ, double posX, double posZ) {
        double closestX = Math.min(Math.abs(positionX.getMin() - posX), Math.abs(positionX.getMax() - posX));
        double closestZ = Math.min(Math.abs(positionZ.getMin() - posZ), Math.abs(positionZ.getMax() - posZ));
        return Math.sqrt(closestX * closestX + closestZ * closestZ);
    }

}
