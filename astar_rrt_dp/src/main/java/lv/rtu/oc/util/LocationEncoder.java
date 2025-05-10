package lv.rtu.oc.util;

public class LocationEncoder {

    public static long encode(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    public static int[] decode(long encoded) {
        int x = (int) (encoded >> 32);
        int y = (int) (encoded & 0xFFFFFFFFL);
        return new int[] { x, y };
    }

}
