package lv.rtu.oc.util;

public class Color {

    public static java.awt.Color getColorForDouble(double value) {
        double normalizedValue = (value - 0) / (2000 - 0);
        if (normalizedValue < 0) normalizedValue = 0;
        if (normalizedValue > 1) normalizedValue = 1;

        return java.awt.Color.getHSBColor((float) normalizedValue, 1.0f, 1.0f);
    }

}
