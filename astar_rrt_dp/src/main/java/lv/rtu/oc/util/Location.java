package lv.rtu.oc.util;

import lv.rtu.oc.util.intervalarithmetic.IntervalDouble;

public class Location {

    public IntervalDouble positionX;
    public IntervalDouble positionZ;

    public Location(IntervalDouble positionX, IntervalDouble positionZ) {
        this.positionX = positionX;
        this.positionZ = positionZ;
    }

}
