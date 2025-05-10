package lv.rtu.oc.data;

import lv.rtu.oc.Main;
import lv.rtu.oc.util.Location;
import lv.rtu.oc.util.intervalarithmetic.IntervalDouble;

import java.awt.*;

public abstract class EnvironmentBase {

    public State initialState;
    public Location goalRegion;

    public EnvironmentBase() {
        initialState = new State();
        goalRegion = new Location(new IntervalDouble(0.0D, 0.0D), new IntervalDouble(0.0D, 0.0D));
    }

    public abstract boolean isValid(State s);

    public double approximateDistanceToFinish(double x, double y) {
        double dx = goalRegion.positionX.contains(x) ? 0 : Math.min(Math.abs(goalRegion.positionX.getMin() - x), Math.abs(goalRegion.positionX.getMax() - x));
        double dz = goalRegion.positionZ.contains(y) ? 0 : Math.min(Math.abs(goalRegion.positionZ.getMin() - y), Math.abs(goalRegion.positionZ.getMax() - y));
        return Math.sqrt(dx*dx + dz*dz);
    }

    public boolean isGoal(double x, double y) {
        return goalRegion.positionX.contains(x) && goalRegion.positionZ.contains(y);
    }

    public abstract void drawEnvironment(Graphics g, double offsetX, double offsetY, double scaleFactor);

    public int findRequiredSteps(State initialState, IntervalDouble a, IntervalDouble w, IntervalDouble d, IntervalDouble s) throws Exception {
        int steps = 0;
        State state = initialState.clone();
        while (steps < 10000) {
            if (state.positionX.collides(Main.environment.goalRegion.positionX) && state.positionZ.collides(Main.environment.goalRegion.positionZ)) return steps;
            steps++;
            state.applyPhysics(a, w, d, s);
        }
        throw new Exception("Nothing found");
    }

}
