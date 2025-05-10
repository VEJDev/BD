package lv.rtu.oc.data;

import lv.rtu.oc.util.Location;
import lv.rtu.oc.util.intervalarithmetic.IntervalDouble;

import java.awt.*;

public class Environment1 extends EnvironmentBase {

    public Environment1() {
        initialState = new State(
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D)
        );
        goalRegion = new Location(new IntervalDouble(12.5D, 13.0D), new IntervalDouble(12.5D, 13.0D));
    }

    @Override
    public boolean isValid(State s) {
        return true;
    }

    @Override
    public void drawEnvironment(Graphics g, double offsetX, double offsetY, double scaleFactor) {
        // initial state
        g.setColor(Color.MAGENTA);
        int x = (int) ((-initialState.positionX.getMin()) * scaleFactor + offsetX);
        int z = (int) (initialState.positionZ.getMin() * scaleFactor + offsetY);
        g.fillOval(x, z, 10, 10);

        // goal region
        g.setColor(Color.GREEN);
        int goalX1 = (int) ((-goalRegion.positionX.getMin()) * scaleFactor + offsetX);
        int goalZ1 = (int) (goalRegion.positionZ.getMin() * scaleFactor + offsetY);
        int goalX2 = (int) ((-goalRegion.positionX.getMax()) * scaleFactor + offsetX);
        int goalZ2 = (int) (goalRegion.positionZ.getMax() * scaleFactor + offsetY);
        g.fillRect(goalX1, goalZ1, goalX2 - goalX1, goalZ2 - goalZ1);
    }

}
