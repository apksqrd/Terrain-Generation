package simple;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.JPanel;

public class Canvas extends JPanel {
    public static Logger logger = Logger.getLogger(Canvas.class.getName());

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
            // logger.warning("New max and min were same.");
            return newMin;
        }
        return newMin + ((point - initialMin) / (initialMax - initialMin)) * (newMax - newMin);
    }

    public static class PixelGrid implements Shape {
        private final int x, y, displayWidth, displayHeight;
        private final int[][] rgbValues;

        public PixelGrid(double[][] heightMap, int x, int y, int displayWidth, int displayHeight) {
            this.rgbValues = new int[heightMap.length][heightMap[0].length];
            for (int row = 0; row < heightMap.length; row++) {
                for (int col = 0; col < heightMap[0].length; col++) {
                    this.rgbValues[row][col] = Color.HSBtoRGB(0, 0,
                            (float) Math.max(0, Math.min(1, heightMap[row][col])));
                }
            }
            this.x = x;
            this.y = y;
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
        }

        public PixelGrid(int[][] rgbValues, int x, int y, int displayWidth, int displayHeight) {
            this.rgbValues = rgbValues;
            this.x = x;
            this.y = y;
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
        }

        @Override
        public void paintOnTo(Graphics g) {
            BufferedImage image = new BufferedImage(rgbValues[0].length, rgbValues.length, BufferedImage.TYPE_INT_ARGB);

            int[] flattenedRgbValue = Arrays.stream(rgbValues).flatMapToInt(Arrays::stream).toArray();
            int[] imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

            System.arraycopy(flattenedRgbValue, 0, imageData, 0, flattenedRgbValue.length);

            g.drawImage(image, x, y, displayWidth, displayHeight, null);
        }
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
