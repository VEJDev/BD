package lv.rtu.oc.algorithms;

import lv.rtu.oc.Main;
import lv.rtu.oc.data.Environment1;
import lv.rtu.oc.data.Environment2;
import lv.rtu.oc.data.Environment3;
import lv.rtu.oc.data.State;
import lv.rtu.oc.util.LocationEncoder;
import lv.rtu.oc.util.intervalarithmetic.IntervalDouble;

import java.util.*;

public class DynamicProgramming implements IAlgorithm {

    private static final IntervalDouble inputInterval = new IntervalDouble(0, 1);

    private final List<Node> finalNodes = new LinkedList<>();

    public static class Node {

        public State state;
        public HashMap<Node, Double> previousNodes;
        public Node bestNextNode;
        public double lowestNextNodeTime = Double.MAX_VALUE;

        public Node(State state, HashMap<Node, Double> previousNodes) {
            this.state = state;
            this.previousNodes = previousNodes;
        }

    }

    @Override
    public void init(Double[][] inputs) {
        Node n0 = new Node(Main.environment.initialState, null);
        List<Node> initialNodeList = new LinkedList<>();
        initialNodeList.add(n0);

        switch (Main.environment) {
            case Environment1 environment1 -> generateNodesEnv1(initialNodeList);
            case Environment2 environment2 -> generateNodesEnv2(initialNodeList);
            case Environment3 environment3 -> generateNodesEnv3(initialNodeList);
            default -> {}
        }

        if (finalNodes.isEmpty()) {
            System.out.println("No final states.");
        } else {
            for (Node n : finalNodes) {
                updateBestNextNodes(n, 0.0D);
            }
            while(n0.bestNextNode != null) {
                System.out.println(n0.state + ", " + n0.lowestNextNodeTime);
                n0 = n0.bestNextNode;
            }
        }

    }

    public void updateBestNextNodes(Node n, double cumulativeSteps) {
        if (n.previousNodes == null) return;
        for (Map.Entry<Node, Double> n2 : n.previousNodes.entrySet()) {
            double steps = cumulativeSteps + n2.getValue();
            if (steps < n2.getKey().lowestNextNodeTime) {
                n2.getKey().lowestNextNodeTime = steps;
                n2.getKey().bestNextNode = n;
            }
            updateBestNextNodes(n2.getKey(), steps);
        }
    }

    // could improve this
    private static double findRequiredStepsToNode(State fromState, State toState) {
        double steps = 0.0D;
        State state = fromState.clone();
        State previousState = state.clone();
        while (steps < 50.0D) {
            if (state.positionX.contains(toState.positionX.getMin()) &&
                    state.positionZ.contains(toState.positionZ.getMin()) &&
                    state.yaw.contains(toState.yaw.getMin()) &&
                    state.deltaRotation.contains(toState.deltaRotation.getMin()) &&
                    state.motionX.contains(toState.motionX.getMin()) &&
                    state.motionZ.contains(toState.motionZ.getMin())
            ) {
                double dist1 = previousState.distanceToOuterLimits(toState.positionX.getMin(), toState.positionZ.getMin());
                double dist2 = state.distanceToOuterLimits(toState.positionX.getMin(), toState.positionZ.getMin());
                return steps - 1 + dist1 / (dist1 + dist2);
            }
            previousState = state.clone();
            state.applyPhysics(inputInterval, inputInterval, inputInterval, inputInterval);
            steps++;
        }
        return Double.MAX_VALUE - 1;
    }

    public void generateNodesEnv1(List<Node> currentNodes) {
        double maxRadius = 18.4D;
        double step = 3.0D;
        double maxSpacing = 0.5D;

        int steps = 0;

        List<Node> nextNodes = new LinkedList<>();
        for (double r = step; r <= maxRadius; r += step) {
            double circumference = 0.5 * Math.PI * r;
            int numPoints = Math.max(1, (int) Math.ceil(circumference / maxSpacing));

            for (int i = 0; i < numPoints; i++) {
                double theta = (0.5 * Math.PI) * i / numPoints;
                double x = r * Math.cos(theta);
                double y = r * Math.sin(theta);
                boolean appendAsFinal = x >= 12.5D && x <= 13.0D && y >= 12.5D && y <= 13.0D;
                for (double motionX = -1.0; motionX<=1.0D; motionX+=0.5) {
                    for (double motionY = -1.0D; motionY<=1.0D; motionY+=0.5) {
                        for (double yaw = -0.0; yaw<=140.0D; yaw+=28.0) {
                            for (double deltaYaw = -0.0D; deltaYaw<=0.0D; deltaYaw+=4.0) {
                                State newState = new State(x, y, motionX, motionY, yaw, deltaYaw);
                                HashMap<Node, Double> map = new HashMap<>();
                                for (Node n : currentNodes) {
                                    double requiredSteps = findRequiredStepsToNode(n.state, newState);
                                    if (requiredSteps < 20) map.put(n, findRequiredStepsToNode(n.state, newState));
                                }
                                Node newNode = new Node(newState, map);
                                nextNodes.add(newNode);
                                if (appendAsFinal) finalNodes.add(newNode);
                                System.out.println(x + ", " + y + ", " + steps + ", " + nextNodes.size());
                            }
                        }
                    }
                }
            }
            currentNodes = nextNodes;
            nextNodes = new LinkedList<>();
            steps++;
        }
    }

    public void generateNodesEnv2(List<Node> currentNodes) {
        double maxRadius = 8.0D;
        double step = 2.0D;
        double maxSpacing = 0.35D;

        int steps = 0;

        List<Node> nextNodes = new LinkedList<>();
        for (double r = step; r <= maxRadius; r += step) {
            double circumference = 0.5 * Math.PI * r;
            int numPoints = Math.max(1, (int) Math.ceil(circumference / maxSpacing));
            for (int i = 0; i < numPoints; i++) {
                double theta = (0.5 * Math.PI) * i / numPoints;
                double x = r * Math.cos(theta);
                double y = r * Math.sin(theta);
                if (x > -0.2 && x < 0.2 && y > 1.0 && y < 2.0) continue;
                boolean appendAsFinal = x >= -0.5D && x <= 0.5D && y >= 5.0D && y <= 20.0D;
                for (double motionX = -1.0; motionX<=1.0D; motionX+=0.5) {
                    for (double motionY = -1.0D; motionY<=1.0D; motionY+=0.5) {
                        for (double yaw = -60.0; yaw<=60.0D; yaw+=20.0) {
                            for (double deltaYaw = -0.0D; deltaYaw<=0.0D; deltaYaw+=4.0) {
                                State newState = new State(x, y, motionX, motionY, yaw, deltaYaw);
                                HashMap<Node, Double> map = new HashMap<>();
                                for (Node n : currentNodes) {
                                    double requiredSteps = findRequiredStepsToNode(n.state, newState);
                                    if (requiredSteps < 20) map.put(n, findRequiredStepsToNode(n.state, newState));
                                }
                                Node newNode = new Node(newState, map);
                                nextNodes.add(newNode);
                                if (appendAsFinal) finalNodes.add(newNode);
                            }
                        }
                    }
                }
                System.out.println(x + ", " + y + ", " + steps);
            }
            currentNodes = nextNodes;
            nextNodes = new LinkedList<>();
            steps++;
        }
    }

    public void generateNodesEnv3(List<Node> currentNodes) {
        Map<Long, Double> environment = ((Environment3)Main.environment).environment;

        // Finding maximum distance to finish
        double currentDistanceToFinish = 0;
        int steps = 0;
        for (double distance : environment.values()) {
            if (distance > currentDistanceToFinish) {
                currentDistanceToFinish = distance;
            }
        }

        List<Node> nextNodes = new LinkedList<>();
        while (currentDistanceToFinish > 0.0D) {
            List<Long> locations = new LinkedList<>();
            for (long encodedLocation : environment.keySet()) {
                double dist = environment.get(encodedLocation);
                if (Math.abs(currentDistanceToFinish - dist) < 1.0) {
                    locations.add(encodedLocation);
                }
            }

            double maxDistance = 0.0;
            int[] p1 = new int[0], p2 = new int[0];

            for (int i = 0; i < locations.size(); i++) {
                for (int j = i + 1; j < locations.size(); j++) {
                    double d = distance(locations.get(i), locations.get(j));
                    if (d > maxDistance) {
                        maxDistance = d;
                        p1 = LocationEncoder.decode(locations.get(i));
                        p2 = LocationEncoder.decode(locations.get(j));
                    }
                }
            }

            for (int i = 1; i <= 10; i++) {
                double t = i / 11.0;
                double x = p1[0] + t * (p2[0] - p1[0]);
                double y = p1[1] + t * (p2[1] - p1[1]);
                boolean appendAsFinal = x >= -4.0D && x <= 4.0D && y >= 1.0D && y <= 5.0D;
                for (double motionX = -1.5D; motionX<=1.5D; motionX+=0.5) {
                    for (double motionY = -1.5D; motionY<=1.5D; motionY+=0.5) {
                        for (double yaw = -180.0; yaw<=180.0D; yaw+=18.0) {
                            for (double deltaYaw = -0.0D; deltaYaw<=0.0D; deltaYaw+=5.0) {
                                State newState = new State(x, y, motionX, motionY, yaw, deltaYaw);
                                HashMap<Node, Double> map = new HashMap<>();
                                for (Node n : currentNodes) {
                                    map.put(n, findRequiredStepsToNode(n.state, newState));
                                }
                                Node newNode = new Node(newState, map);
                                nextNodes.add(newNode);
                                if (appendAsFinal) finalNodes.add(newNode);
                            }
                        }
                    }
                }
                System.out.println(x + ", " + y + ", " + steps);
            }
            currentNodes = nextNodes;
            nextNodes = new LinkedList<>();
            steps++;
            currentDistanceToFinish -= 4.0D;
        }
    }

    public static double distance(long a, long b) {
        int[] c = LocationEncoder.decode(a);
        int[] d = LocationEncoder.decode(b);
        int dx = c[0]-d[0];
        int dy = c[1]-d[1];
        return Math.sqrt(dx*dx+dy*dy);
    }

}