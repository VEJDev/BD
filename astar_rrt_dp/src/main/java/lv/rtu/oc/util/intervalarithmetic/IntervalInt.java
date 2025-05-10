package lv.rtu.oc.util.intervalarithmetic;

public class IntervalInt {

    private int min;
    private int max;

    public IntervalInt(int value) {
        this.min = value;
        this.max = value;
    }

    public IntervalInt(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public IntervalInt add(IntervalInt interval) {
        this.min += interval.min;
        this.max += interval.max;
        return this;
    }

    public IntervalInt add(int value) {
        this.min += value;
        this.max += value;
        return this;
    }

    public IntervalInt subtract(IntervalInt interval) {
        this.min -= interval.max;
        this.max -= interval.min;
        return this;
    }

    public IntervalInt subtract(int value) {
        this.min -= value;
        this.max -= value;
        return this;
    }

    public IntervalInt multiply(IntervalInt interval) {
        int val1 = this.min * interval.min;
        int val2 = this.min * interval.max;
        int val3 = this.max * interval.min;
        int val4 = this.max * interval.max;
        this.min = Math.min(Math.min(val1, val2), Math.min(val3, val4));
        this.max = Math.max(Math.max(val1, val2), Math.max(val3, val4));
        return this;
    }

    public IntervalInt multiply(int value) {
        int val1 = this.min * value;
        int val2 = this.max * value;
        this.min = Math.min(val1, val2);
        this.max = Math.max(val1, val2);
        return this;
    }

    public IntervalInt divide(IntervalInt interval) {
        if (interval.min <= 0 && interval.max >= 0) {
            throw new ArithmeticException("Division by an interval containing zero is undefined.");
        }
        int val1 = this.min / interval.min;
        int val2 = this.min / interval.max;
        int val3 = this.max / interval.min;
        int val4 = this.max / interval.max;
        this.min = Math.min(Math.min(val1, val2), Math.min(val3, val4));
        this.max = Math.max(Math.max(val1, val2), Math.max(val3, val4));
        return this;
    }

    public IntervalInt divide(int value) {
        if (value == 0) {
            throw new ArithmeticException("Division by zero is undefined.");
        }
        int val1 = this.min / value;
        int val2 = this.max / value;
        this.min = Math.min(val1, val2);
        this.max = Math.max(val1, val2);
        return this;
    }

    // TODO: Figure out how to make this better, returns new object
    public IntervalInt getSinValue() {
        return new IntervalInt(-1, 1);
    }

    // TODO: Figure out how to make this better, returns new object
    public IntervalInt getCosValue() {
        return new IntervalInt(-1, 1);
    }

    // TODO: Implement this
    public IntervalInt getTanValue() {
        return this;
    }

    public IntervalInt pow(IntervalInt interval) {
        return this;
    }

    public IntervalInt pow(int value) {
        return this;
    }

    public IntervalDouble convertToDouble() {
        return new IntervalDouble(min, max);
    }

    public IntervalInt merge(IntervalInt interval) {
        this.min = Math.min(interval.min, min);
        this.max = Math.min(interval.max, max);
        return this;
    }

    public IntervalInt set(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public IntervalInt setMin(int min) {
        this.min = min;
        return this;
    }

    public IntervalInt setMax(int max) {
        this.max = max;
        return this;
    }

    public boolean contains(int value) {
        return value >= min && value <= max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

}
