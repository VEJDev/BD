package lv.rtu.oc;

import lv.rtu.oc.algorithms.DynamicProgramming;
import lv.rtu.oc.algorithms.IAlgorithm;
import lv.rtu.oc.data.*;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static final EnvironmentBase environment = new Environment3();
    public static final ReferencePath referencePath = new ReferencePath("C:\\Users\\user\\Desktop\\paths\\46_45.csv", Color.YELLOW);
    public static final List<State> exploredStates = new LinkedList<>();

    private static final Double[][] inputs = {
            {0.0D, 0.0D, 0.0D, 0.0D}, // No key
            {0.0D, 0.0D, 0.0D, 1.0D}, // S key
            {0.0D, 0.0D, 1.0D, 0.0D}, // D key
            {0.0D, 1.0D, 0.0D, 0.0D}, // W key
            {1.0D, 0.0D, 0.0D, 0.0D}, // A key
            {1.0D, 1.0D, 0.0D, 0.0D}, // A+W key
            {0.0D, 1.0D, 1.0D, 0.0D}, // W+S key
            {0.0D, 0.0D, 1.0D, 1.0D}, // D+S key
            {1.0D, 0.0D, 0.0D, 1.0D}  // A+S key
    };

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        IAlgorithm algorithm = new DynamicProgramming();
        algorithm.init(inputs);
        System.out.println("Algorithm finished in: " + (System.currentTimeMillis() - start));

    }

}
