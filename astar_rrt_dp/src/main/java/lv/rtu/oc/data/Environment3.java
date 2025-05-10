package lv.rtu.oc.data;

import lv.rtu.oc.Main;
import lv.rtu.oc.util.Location;
import lv.rtu.oc.util.LocationEncoder;
import lv.rtu.oc.util.intervalarithmetic.IntervalDouble;
import lv.rtu.oc.util.Color;

import java.awt.*;
import java.io.*;
import java.util.*;

public class Environment3 extends EnvironmentBase {

    public Map<Long, Double> environment = new HashMap<>();
    public LinkedList<Long> endNodes = new LinkedList<>();

    public Environment3() {
        initialState = new State(
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(-180.0D, -180.0D),
                new IntervalDouble(0.0D, 0.0D)
        );
        goalRegion = new Location(new IntervalDouble(-3.0D, 4.0D), new IntervalDouble(1.0D, 2.0D));
        System.out.println("Initializing the environment...");
        endNodes.add(LocationEncoder.encode(-3, 1));
        endNodes.add(LocationEncoder.encode(-2, 1));
        endNodes.add(LocationEncoder.encode(-1, 1));
        endNodes.add(LocationEncoder.encode(0, 1));
        endNodes.add(LocationEncoder.encode(1, 1));
        endNodes.add(LocationEncoder.encode(2, 1));
        endNodes.add(LocationEncoder.encode(3, 1));
        //importEnvironment();
        //updateEnvironmentDistances();
        importEnvironmentAndDistances();
    }

    @Override
    public boolean isValid(State s) {
        return environment.get(LocationEncoder.encode((int)s.positionX.getMin(), (int)s.positionZ.getMin())) != null;
    }

    private void importEnvironmentAndDistances() {
        System.out.println("Importing the environment...");
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\user\\Desktop\\output2.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                int x = Integer.parseInt(row[0]);
                int y = Integer.parseInt(row[1]);
                double d = Double.parseDouble(row[2]);
                environment.put(LocationEncoder.encode(x, y), d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished importing...");
    }

    private void importEnvironment() {
        System.out.println("Importing the environment...");
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\user\\Desktop\\output.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                int x = Integer.parseInt(row[0]);
                int y = Integer.parseInt(row[1]);
                environment.put(LocationEncoder.encode(x, y), 0.0D);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished importing...");
    }

    private void updateEnvironmentDistances() {
        System.out.println("Updating environment distances to the end...");
        int i = 0;
        for (long encodedLoc : environment.keySet()) {
            i++;
            int[] decodedLoc = LocationEncoder.decode(encodedLoc); // start location
            double lowestDistance = -1;
            for (long encodedLocEnd : endNodes) {
                int[] decodedLocEnd = LocationEncoder.decode(encodedLocEnd); // end location
                double shortestPathDistance = findShortestPathDistance(decodedLoc[0], decodedLoc[1], decodedLocEnd[0], decodedLocEnd[1]);
                if (lowestDistance == -1 || lowestDistance > shortestPathDistance) {
                    lowestDistance = shortestPathDistance;
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\user\\Desktop\\output2.csv", true))) {
                writer.write(decodedLoc[0] + "," + decodedLoc[1] + "," + lowestDistance + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            environment.put(encodedLoc, lowestDistance);
            System.out.println(i + "/" + environment.size());
        }
        System.out.println("Finished updating environment distances to the end...");
    }

    private double findShortestPathDistance(int startX, int startY, int endX, int endY) {
        Node start = new Node(startX, startY);
        Node goal = new Node(endX, endY);
        PriorityQueue<Node> openList = new PriorityQueue<>();
        ArrayList<Long> openListLocations = new ArrayList<>();
        Set<Long> closedList = new HashSet<>();

        start.g = 0;
        start.f = heuristic(start, goal);
        openList.add(start);
        openListLocations.add(LocationEncoder.encode(start.x, start.y));

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            openListLocations.remove(LocationEncoder.encode(currentNode.x, currentNode.y));

            if (currentNode.x == goal.x && currentNode.y == goal.y) {
                double dist = 0.0;
                while (currentNode != null) {
                    if (currentNode.parent != null) {
                        dist += heuristic(currentNode, currentNode.parent);
                    }
                    currentNode = currentNode.parent;
                }
                return dist;
            }

            closedList.add(LocationEncoder.encode(currentNode.x, currentNode.y));

            for (int[] direction : new int[][]{{-1, 0}, {-1, -1}, {1, 0}, {1, 1}, {0, -1}, {1, -1}, {-1, 1}, {0, 1}}) {
                int newX = currentNode.x + direction[0];
                int newY = currentNode.y + direction[1];
                long newLocation = LocationEncoder.encode(newX, newY);

                if (!openListLocations.contains(newLocation) && environment.get(newLocation) != null && (!endNodes.contains(newLocation) || newY < currentNode.y) && !closedList.contains(newLocation)) { // might need to be altered based on the direction
                    Node neighbor = new Node(newX, newY);
                    double tentativeG = currentNode.g + 1.0;
                    if (tentativeG < neighbor.g) {
                        neighbor.parent = currentNode;
                        neighbor.g = tentativeG;
                        neighbor.h = heuristic(neighbor, goal);
                        neighbor.f = neighbor.g + neighbor.h;

                        openList.add(neighbor);
                        openListLocations.add(LocationEncoder.encode(neighbor.x, neighbor.y));
                    }
                }
            }
        }

        return Double.MAX_VALUE;
    }

    public class Node implements Comparable<Node> {
        int x, y;
        double g, h, f;
        Node parent;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.g = Integer.MAX_VALUE;
            this.h = 0;
            this.f = Integer.MAX_VALUE;
            this.parent = null;
        }

        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);  // Use Double.compare for floating-point values
        }
    }

    public static double heuristic(Node a, Node b) {
        return Math.sqrt((a.x - b.x)*(a.x - b.x) + (a.y - b.y)*(a.y - b.y));
    }

    /*public double pointDistanceToGoal(double x, double y) {
        double finalDist = Double.MAX_VALUE;
        LinkedList<Long> ll = new LinkedList<>();
        for (int i = (int)x - 1; i < (int)x + 1 ; i++) {
            for (int j = (int)y - 1; j < (int)y + 1 ; j++) {
                double dist;
                long loc = LocationEncoder.encode(i, j);
                if (this.environment.containsKey(loc)) {
                    dist = this.environment.get(loc);
                } else {
                    continue;
                }
                if (finalDist > dist) {
                    ll.clear();
                    finalDist = dist;
                    ll.add(loc);
                } else if (finalDist == dist) {
                    ll.add(loc);
                }
            }
        }
        double dist2 = Double.MAX_VALUE;
        for (long nodeLocation : ll) {
            int[] a = LocationEncoder.decode(nodeLocation);
            double dist = Math.sqrt((x - a[0])*(x - a[0]) + (y - a[1])*(y - a[1]));
            if (dist < dist2) {
                dist2 = dist;
            }
        }
        return finalDist + dist2;
    }*/
    @Override
    public double approximateDistanceToFinish(double x, double y) {
        double finalDist = Double.MAX_VALUE;
        for (int i = (int)x - 1; i < (int)x + 1 ; i++) {
            for (int j = (int)y - 1; j < (int)y + 1 ; j++) {
                long loc = LocationEncoder.encode(i, j);
                if (this.environment.containsKey(loc)) {
                    double dist = this.environment.get(loc);
                    if (dist < finalDist) {
                        finalDist = dist;
                    }
                }
            }
        }
        return finalDist;
    }

    @Override
    public void drawEnvironment(Graphics g, double offsetX, double offsetY, double scaleFactor) {
        // environment
        for (long key : environment.keySet()) {
            g.setColor(Color.getColorForDouble(environment.get(key)));
            int[] a = LocationEncoder.decode(key);
            g.drawRect((int)(((double)(-a[0]) - 1) * scaleFactor + offsetX), (int)(((double)(a[1]) - 1) * scaleFactor + offsetY), (int)scaleFactor, (int)scaleFactor);
        }

        // initial state
        g.setColor(java.awt.Color.MAGENTA);
        int x = (int) ((-initialState.positionX.getMin()) * scaleFactor + offsetX);
        int z = (int) (initialState.positionZ.getMin() * scaleFactor + offsetY);
        g.fillOval(x, z, 10, 10);

        // goal region
        g.setColor(java.awt.Color.GREEN);
        int goalX1 = (int) ((-goalRegion.positionX.getMin()) * scaleFactor + offsetX);
        int goalZ1 = (int) (goalRegion.positionZ.getMin() * scaleFactor + offsetY);
        int goalX2 = (int) ((-goalRegion.positionX.getMax()) * scaleFactor + offsetX);
        int goalZ2 = (int) (goalRegion.positionZ.getMax() * scaleFactor + offsetY);
        g.fillRect(goalX1, goalZ1, goalX2 - goalX1, goalZ2 - goalZ1);
    }

    @Override
    public int findRequiredSteps(State initialState, IntervalDouble a, IntervalDouble w, IntervalDouble d, IntervalDouble s) throws Exception {
        int steps = 0;
        if (Main.environment.goalRegion.positionX.contains(initialState.positionX.getMin()) && Main.environment.goalRegion.positionZ.contains(initialState.positionZ.getMax())) return 0;
        double distToGoal = Main.environment.approximateDistanceToFinish(initialState.positionX.getMin(), initialState.positionZ.getMin());
        State state = initialState.clone();
        double traveledDist = 0;
        label:
        while (steps < 1500) {
            double dxsq = Math.max(Math.pow(state.positionX.getMax() - initialState.positionX.getMin(), 2), Math.pow(state.positionX.getMin() - initialState.positionX.getMax(), 2));
            double dysq = Math.max(Math.pow(state.positionZ.getMax() - initialState.positionZ.getMin(), 2), Math.pow(state.positionZ.getMin() - initialState.positionZ.getMax(), 2));
            traveledDist = Math.sqrt(dxsq+dysq);
            if (distToGoal < traveledDist) {
                return steps;
            } else {
                steps++;
            }
            state.applyPhysics(a, w, d, s);
            if ((state.positionX.getMax() - state.positionX.getMin()) * (state.positionZ.getMax() - state.positionZ.getMin()) < 16) {
                int g = 0;
                int h = 0;
                for (int i = (int)state.positionX.getMin(); i < (int)Math.ceil(state.positionX.getMax()); i++) {
                    for (int j = (int)state.positionZ.getMin(); j < (int)Math.ceil(state.positionZ.getMax()); j++) {
                        g++;
                        if (environment.containsKey(LocationEncoder.encode(i, j))) {
                            continue label ;
                        } else {
                            h++;
                        }
                    }
                }
                if (g!=h) return Integer.MAX_VALUE;
            }
        }
        throw new Exception("Nothing found" + state + ", " + distToGoal + ", " + traveledDist);
    }

}
