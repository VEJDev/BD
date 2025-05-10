package lv.rtu.oc.data;

import lv.rtu.oc.util.intervalarithmetic.IntervalDouble;

public class State {

    public IntervalDouble positionX;
    public IntervalDouble positionZ;
    public IntervalDouble motionX;
    public IntervalDouble motionZ;
    public IntervalDouble yaw;
    public IntervalDouble deltaRotation;

    public State(IntervalDouble positionX, IntervalDouble positionZ, IntervalDouble motionX, IntervalDouble motionZ, IntervalDouble yaw, IntervalDouble deltaRotation) {
        this.positionX = positionX;
        this.positionZ = positionZ;
        this.motionX = motionX;
        this.motionZ = motionZ;
        this.yaw = yaw;
        this.deltaRotation = deltaRotation;
    }

    public State(double positionX, double positionZ, double motionX, double motionZ, double yaw, double deltaRotation) {
        this.positionX = new IntervalDouble(positionX, positionX);
        this.positionZ = new IntervalDouble(positionZ, positionZ);
        this.motionX = new IntervalDouble(motionX, motionX);
        this.motionZ = new IntervalDouble(motionZ, motionZ);
        this.yaw = new IntervalDouble(yaw, yaw);
        this.deltaRotation = new IntervalDouble(deltaRotation, deltaRotation);
    }

    public State() {
        positionX = new IntervalDouble(0.0D, 0.0D);
        positionZ = new IntervalDouble(0.0D, 0.0D);
        motionX = new IntervalDouble(0.0D, 0.0D);
        motionZ = new IntervalDouble(0.0D, 0.0D);
        yaw = new IntervalDouble(0.0D, 0.0D);
        deltaRotation = new IntervalDouble(0.0D, 0.0D);
    }

    public State clone() {
        return new State(
                new IntervalDouble(this.positionX.getMin(), this.positionX.getMax()),
                new IntervalDouble(this.positionZ.getMin(), this.positionZ.getMax()),
                new IntervalDouble(this.motionX.getMin(), this.motionX.getMax()),
                new IntervalDouble(this.motionZ.getMin(), this.motionZ.getMax()),
                new IntervalDouble(this.yaw.getMin(), this.yaw.getMax()),
                new IntervalDouble(this.deltaRotation.getMin(), this.deltaRotation.getMax())
        );
    }

    @Override
    public String toString() {
        int outputFormat = 1;
        if (outputFormat == 1) {
            return String.format(
                    "posX=[%f,%f], posZ=[%f,%f], motX=[%f,%f], motZ=[%f,%f], yaw=[%f,%f], deltaRot=[%f,%f]",
                    positionX.getMin(),
                    positionX.getMax(),
                    positionZ.getMin(),
                    positionZ.getMax(),
                    motionX.getMin(),
                    motionX.getMax(),
                    motionZ.getMin(),
                    motionZ.getMax(),
                    yaw.getMin(),
                    yaw.getMax(),
                    deltaRotation.getMin(),
                    deltaRotation.getMax()
            );
        } else if (outputFormat == 2) {
            return String.format(
                    "polygon((%f,%f),(%f,%f),(%f,%f),(%f,%f))",
                    positionX.getMin(),
                    positionZ.getMin(),
                    positionX.getMax(),
                    positionZ.getMin(),
                    positionX.getMax(),
                    positionZ.getMax(),
                    positionX.getMin(),
                    positionZ.getMax()
            );
        } else if (outputFormat == 3) {
            return String.format(
                    "%f,%f",
                    positionX.getMin(),
                    positionZ.getMin()
            );
        }
        return "";
    }

    public double distanceTo(State other) {
        return Math.sqrt(Math.pow(this.positionX.getMin() - other.positionX.getMin(), 2) + Math.pow(this.positionZ.getMin() - other.positionZ.getMin(), 2));
    }

    public State applyPhysics(IntervalDouble a, IntervalDouble w, IntervalDouble d, IntervalDouble s) {
        IntervalDouble f =
                a.notEquals(d) && w.equals(0.0D) && s.equals(0.0D) ? new IntervalDouble(0.005D, 0.005D) : a.equals(d) || w.notEquals(0.0D) || s.notEquals(0.0D) ? new IntervalDouble(0.0D, 0.0D) : new IntervalDouble(0.0D, 0.005D);
        f = w.equals(1.0D) ? f.add(0.04D) : w.notEquals(1.0D) ? f : new IntervalDouble(f.getMin(), f.getMax() + 0.04);
        f = s.equals(1.0D) ? f.subtract(0.005D) : s.notEquals(1.0D) ? f : new IntervalDouble(f.getMin() - 0.005D, f.getMax());

        if (a.equals(1.0D)) {
            deltaRotation.multiply(0.989D).add(-1.0D);
        } else if (d.equals(1.0D)) {
            deltaRotation.multiply(0.989D).add(1.0D);
        } else if (a.notEquals(1.0D) && d.notEquals(1.0D)) {
            deltaRotation.multiply(0.989D);
        } else {
            deltaRotation.multiply(0.989D).add(new IntervalDouble(-1.0D, 1.0D));
        }

        yaw.add(deltaRotation);
        motionX.multiply(0.989D).add(yaw.getSinValue().multiply(f));
        motionZ.multiply(0.989D).add(yaw.getCosValue().multiply(f));
        positionX.add(motionX);
        positionZ.add(motionZ);
        return this;
    }

    public double distanceToOuterLimits(double x, double y) {
        boolean inside = (x >= positionX.getMin() && x <= positionX.getMax() && y >= positionZ.getMin() && y <= positionZ.getMax());

        if (inside) {
            double dx = Math.min(Math.abs(x - positionX.getMin()), Math.abs(x - positionX.getMax()));
            double dy = Math.min(Math.abs(y - positionZ.getMin()), Math.abs(y - positionZ.getMax()));
            return Math.min(dx, dy);
        } else {
            double clampedX = Math.max(positionX.getMin(), Math.min(x, positionX.getMax()));
            double clampedY = Math.max(positionZ.getMin(), Math.min(y, positionZ.getMax()));
            double dx = x - clampedX;
            double dy = y - clampedY;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }
}
