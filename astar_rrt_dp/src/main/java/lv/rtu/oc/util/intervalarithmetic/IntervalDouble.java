package lv.rtu.oc.util.intervalarithmetic;

public class IntervalDouble {

    private double min;
    private double max;

    public IntervalDouble(double value) {
        this.min = value;
        this.max = value;
    }

    public IntervalDouble(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public IntervalDouble add(IntervalDouble interval) {
        this.min += interval.min;
        this.max += interval.max;
        return this;
    }

    public IntervalDouble add(double value) {
        this.min += value;
        this.max += value;
        return this;
    }

    public IntervalDouble subtract(IntervalDouble interval) {
        this.min -= interval.max;
        this.max -= interval.min;
        return this;
    }

    public IntervalDouble subtract(double value) {
        this.min -= value;
        this.max -= value;
        return this;
    }

    public IntervalDouble multiply(IntervalDouble interval) {
        double val1 = this.min * interval.min;
        double val2 = this.min * interval.max;
        double val3 = this.max * interval.min;
        double val4 = this.max * interval.max;
        this.min = Math.min(Math.min(val1, val2), Math.min(val3, val4));
        this.max = Math.max(Math.max(val1, val2), Math.max(val3, val4));
        return this;
    }

    public IntervalDouble multiply(double value) {
        double val1 = this.min * value;
        double val2 = this.max * value;
        this.min = Math.min(val1, val2);
        this.max = Math.max(val1, val2);
        return this;
    }

    public IntervalDouble divide(IntervalDouble interval) {
        if (interval.min <= 0 && interval.max >= 0) {
            throw new ArithmeticException("Division by an interval containing zero is undefined.");
        }
        double val1 = this.min / interval.min;
        double val2 = this.min / interval.max;
        double val3 = this.max / interval.min;
        double val4 = this.max / interval.max;
        this.min = Math.min(Math.min(val1, val2), Math.min(val3, val4));
        this.max = Math.max(Math.max(val1, val2), Math.max(val3, val4));
        return this;
    }

    public IntervalDouble divide(double value) {
        if (value == 0) {
            throw new ArithmeticException("Division by zero is undefined.");
        }
        double val1 = this.min / value;
        double val2 = this.max / value;
        this.min = Math.min(val1, val2);
        this.max = Math.max(val1, val2);
        return this;
    }

    // returns new object
    public IntervalDouble getSinValue() {
        double sinA = Math.sin(Math.toRadians(min));
        double sinB = Math.sin(Math.toRadians(max));

        double lowerBound = Math.min(sinA, sinB);
        double upperBound = Math.max(sinA, sinB);

        double criticalPoint1 = 90;
        double criticalPoint2 = 270;
        double period = 360;

        double normalizedCriticalPoint1 = criticalPoint1 + Math.ceil((min - criticalPoint1) / period) * period;
        if (normalizedCriticalPoint1 > max) {
            normalizedCriticalPoint1 -= period;
        }

        double normalizedCriticalPoint2 = criticalPoint2 + Math.ceil((min - criticalPoint2) / period) * period;
        if (normalizedCriticalPoint2 > max) {
            normalizedCriticalPoint2 -= period;
        }

        if (normalizedCriticalPoint1 >= min && normalizedCriticalPoint1 <= max) {
            upperBound = 1.0;
        }
        if (normalizedCriticalPoint2 >= min && normalizedCriticalPoint2 <= max) {
            lowerBound = -1.0;
        }

        return new IntervalDouble(lowerBound, upperBound);
    }

    // returns new object
    public IntervalDouble getCosValue() {
        double cosA = Math.cos(Math.toRadians(min));
        double cosB = Math.cos(Math.toRadians(max));

        double lowerBound = Math.min(cosA, cosB);
        double upperBound = Math.max(cosA, cosB);

        double criticalPoint1 = 0.0;
        double criticalPoint2 = 180;
        double period = 360;

        double normalizedCriticalPoint1 = criticalPoint1 + Math.ceil((min - criticalPoint1) / period) * period;
        if (normalizedCriticalPoint1 > max) {
            normalizedCriticalPoint1 -= period;
        }

        double normalizedCriticalPoint2 = criticalPoint2 + Math.ceil((min - criticalPoint2) / period) * period;
        if (normalizedCriticalPoint2 > max) {
            normalizedCriticalPoint2 -= period;
        }

        if (normalizedCriticalPoint1 >= min && normalizedCriticalPoint1 <= max) {
            upperBound = 1.0;
        }
        if (normalizedCriticalPoint2 >= min && normalizedCriticalPoint2 <= max) {
            lowerBound = -1.0;
        }

        return new IntervalDouble(lowerBound, upperBound);
    }

    // TODO: Implementation
    public IntervalDouble getTanValue() {
        return null;
    }

    // TODO: Implementation
    public IntervalDouble pow(IntervalDouble interval) {
        return null;
    }

    // TODO: Implementation
    public IntervalDouble pow(double value) {
        return null;
    }

    public IntervalInt convertToInt() {
        return new IntervalInt((int)min, (int)Math.ceil(max));
    }

    public IntervalDouble merge(IntervalDouble interval) {
        this.min = Math.min(interval.min, min);
        this.max = Math.max(interval.max, max);
        return this;
    }

    public IntervalDouble set(double min, double max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public IntervalDouble setMin(double min) {
        this.min = min;
        return this;
    }

    public IntervalDouble setMax(double max) {
        this.max = max;
        return this;
    }

    public boolean contains(double value) {
        return value >= min && value <= max;
    }

    // Precise equality check, don't use !equals() use notEquals() instead
    public boolean equals(double value) {
        return min == value && max == value;
    }

    public boolean equals(IntervalDouble interval) {
        return min == interval.min && max == interval.max;
    }

    public boolean notEquals(double value) {
        return value > max || value < min;
    }

    public boolean notEquals(IntervalDouble interval) {
        return min != interval.min || max != interval.max;
    }

    public boolean collides(IntervalDouble interval) {
        return max >= interval.min && min <= interval.max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public String toString() {
        return "[" + min + ", " + max + "]";
    }

}
