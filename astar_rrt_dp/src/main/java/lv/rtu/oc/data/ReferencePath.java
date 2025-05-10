package lv.rtu.oc.data;

import lv.rtu.oc.util.Line;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ReferencePath {

    public List<Line> referencePath = new ArrayList<>();
    public Color color;

    public ReferencePath(String path, Color color) {
        this.color = color;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            double prevX = 0.0D, prevY = 0.0D;
            boolean a = false;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                double x = Double.parseDouble(row[0].replaceAll("[^\\dEe+\\-\\.]", ""));
                double y = Double.parseDouble(row[1].replaceAll("[^\\dEe+\\-\\.]", ""));
                if (a) referencePath.add(new Line(x, y, prevX, prevY));
                a = true;
                prevX = x;
                prevY = y;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double closestDistanceToReferenceLine(State state) {
        if (!referencePath.isEmpty()) {
            double minDistance = referencePath.getFirst().distanceToPoint(state.positionX.getMin(), state.positionZ.getMin());
            for (Line line : referencePath) {
                double dist = line.distanceToPoint(state.positionX.getMin(), state.positionZ.getMin());
                if (dist < minDistance) {
                    minDistance = dist;
                }
            }
            return minDistance;
        } else {
            return 0;
        }
    }

}
