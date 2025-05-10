package lv.rtu.oc.util;

public class Line {

    public double x1;
    public double z1;
    public double x2;
    public double z2;

    public Line(double x1, double z1, double x2, double z2) {
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
    }

    public double distanceToPoint(double px, double pz) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        double lengthSquared = dx * dx + dz * dz;
        if (lengthSquared == 0) {
            return Math.sqrt((px - x1) * (px - x1) + (pz - z1) * (pz - z1));
        }
        double t = ((px - x1) * dx + (pz - z1) * dz) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        double closestX = x1 + t * dx;
        double closestZ = z1 + t * dz;
        return Math.sqrt((px - closestX) * (px - closestX) + (pz - closestZ) * (pz - closestZ));
    }

}
