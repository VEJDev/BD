package lv.rtu.oc.data;

import lv.rtu.oc.util.Location;
import lv.rtu.oc.util.intervalarithmetic.IntervalDouble;

import java.awt.*;

public class Environment2 extends EnvironmentBase {

    public Environment2() {
        initialState = new State(
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D),
                new IntervalDouble(0.0D, 0.0D)
        );
        goalRegion = new Location(new IntervalDouble(-0.5D, 0.5D), new IntervalDouble(5.0D, 20.0D));
    }

    @Override
    public boolean isValid(State s) {
        return !(s.positionZ.getMin() >= 1.0D && s.positionZ.getMax() <= 2.0D && s.positionX.getMin() > -0.2D && s.positionX.getMax() < 0.2D);
    }

    @Override
    public void drawEnvironment(Graphics g, double offsetX, double offsetY, double scaleFactor) {
        // constraint
        g.setColor(Color.RED);
        int constraintX1 = (int) ((0.2D) * scaleFactor + offsetX);
        int constraintZ1 = (int) (scaleFactor + offsetY);
        int constraintX2 = (int) ((-0.2D) * scaleFactor + offsetX);
        int constraintZ2 = (int) (2.0D * scaleFactor + offsetY);
        g.fillRect(constraintX1, constraintZ1, constraintX2 - constraintX1, constraintZ2 - constraintZ1);

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
