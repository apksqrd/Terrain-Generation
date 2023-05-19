package simple;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JPanel;

public class Canvas extends JPanel {
    public static Logger logger;

    public static interface Shape {
        public void paintOnTo(Graphics g);
    }

    public static double mapToRange(double point, double initialMin, double initialMax, double newMin,
            double newMax) {
        if (initialMax == initialMin) {
            initialMin = 0;
            initialMax = 1;
            logger.warning("Initial max and min were same.");
        }
        if (newMax == newMin) {
            logger.warning("New max and min were same.");
            return newMin;
        }
        return newMin + ((point - initialMin) / (initialMax - initialMin)) * (newMax - newMin);
    }

    public static class Polygon implements Shape {
        private Color color = Color.BLACK;

        private final int[] xPoints, yPoints;

        public Polygon(double[] heightMap, int viewWidth, int viewHeight, int heightMapMin,
                int heightMapMax) {
            xPoints = new int[heightMap.length + 2];
            yPoints = new int[heightMap.length + 2];

            for (int i = 0; i < heightMap.length; i++) {
                xPoints[i] = i * viewWidth / (heightMap.length - 1);
                yPoints[i] = (int) Math.floor(mapToRange(heightMap[i], heightMapMin, heightMapMax, 0, viewHeight));
            }

            // add points at the bottom
            xPoints[xPoints.length - 2] = viewWidth;
            yPoints[yPoints.length - 2] = 0;
            xPoints[xPoints.length - 1] = 0;
            yPoints[yPoints.length - 1] = 0;

            for (int i = 0; i < yPoints.length; i++) {
                // flip all points horizontally because this is cs
                yPoints[i] = viewHeight - yPoints[i];
            }
        }

        public Polygon(int[] xPoints, int[] yPoints) {
            if (xPoints.length != yPoints.length) {
                throw new IllegalArgumentException();
            }

            this.xPoints = xPoints;
            this.yPoints = yPoints;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        public void paintOnTo(Graphics g) {
            g.setColor(color);
            g.fillPolygon(xPoints, yPoints, xPoints.length);
        }
    }

    private final ArrayList<Shape> shapes;

    public Canvas() {
        shapes = new ArrayList<>();
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
        repaint();
    }

    public void clear() {
        shapes.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Shape shape : shapes) {
            shape.paintOnTo(g);
        }
    }

    @Deprecated
    @Override
    public Dimension getMinimumSize() {
        return getSize();
    }

    @Deprecated
    @Override
    public Dimension getMaximumSize() {
        return getSize();
    }

    @Deprecated
    @Override
    public Dimension getPreferredSize() {
        return getSize();
    }
}
