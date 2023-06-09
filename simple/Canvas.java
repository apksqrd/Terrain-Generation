package simple;

import static simple.MathUtils.mapToRange;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.logging.Logger;

import javax.swing.JPanel;

/**
 * Some might say that this code has too many inner classes. I agree, but my
 * excuse is that having all this in one file is simpler because I don't want to
 * make this into a whole library.
 * 
 * (Actually, that is not a bad idea for my next project...)
 * 
 * TODO: for all of the shapes like graph, just get rid of the display x y width
 * height options. Just do fullscreen.
 * ^ I tried that and getting the dimensions of a Graphics object is actually
 * not as easy as I thought
 */
public class Canvas extends JPanel {
    public static Logger logger = Logger.getLogger(Canvas.class.getName());

    public static interface Shape {
        public void paintOnTo(Graphics g);
    }

    public static class ColorGradient implements DoubleToIntFunction {
        /**
         * If i actually make this a library, this and colorgradient could both inherit
         * some other class
         */
        public static class SingularColorGradient {
            private double startHeight, endHeight;
            private int startColor, endColor;

            public SingularColorGradient(double startHeight, double endHeight, Color startColor, Color endColor) {
                this.startHeight = startHeight;
                this.endHeight = endHeight;
                this.startColor = startColor.getRGB();
                this.endColor = endColor.getRGB();
            }

            public SingularColorGradient(double startHeight, double endHeight, int startColor, int endColor) {
                this.startHeight = startHeight;
                this.endHeight = endHeight;
                this.startColor = startColor;
                this.endColor = endColor;
            }

            public boolean isInRange(double height) {
                return startHeight <= height && height < endHeight;
            }

            /**
             * 0 is first color
             * 1 is second color
             */
            public static int mixColor(double blend, int firstColor, int secondColor) {
                int A_DIGITS = 0xFF000000, R_DIGITS = 0x00FF0000, G_DIGITS = 0x0000FF00, B_DIGITS = 0x000000FF;

                double firstColorAmount = 1 - blend, secondColorAmount = 1 - firstColorAmount;

                // [0,256) I think
                double alphaDouble = ((firstColor & A_DIGITS) >>> 24) * firstColorAmount
                        + ((secondColor & A_DIGITS) >>> 24) * secondColorAmount;
                double redDouble = ((firstColor & R_DIGITS) >>> 16) * firstColorAmount
                        + ((secondColor & R_DIGITS) >>> 16) * secondColorAmount;
                double greenDouble = ((firstColor & G_DIGITS) >>> 8) * firstColorAmount
                        + ((secondColor & G_DIGITS) >>> 8) * secondColorAmount;
                double blueDouble = ((firstColor & B_DIGITS) >>> 0) * firstColorAmount
                        + ((secondColor & B_DIGITS) >>> 0) * secondColorAmount;

                // get those to [0, 1)
                alphaDouble /= 255;
                redDouble /= 255;
                greenDouble /= 255;
                blueDouble /= 255;

                return new Color((float) redDouble, (float) greenDouble, (float) blueDouble, (float) alphaDouble)
                        .getRGB();
            }

            public int getColor(double height) {
                return mixColor(MathUtils.mapToRange(height, startHeight, endHeight, 0, 1), startColor, endColor);
            }
        }

        private SingularColorGradient[] subGradients;

        public ColorGradient(SingularColorGradient... subGradient) {
            this.subGradients = subGradient;
        }

        public int getColor(double height) {
            for (SingularColorGradient subGradient : subGradients) {
                if (subGradient.isInRange(height)) {
                    return subGradient.getColor(height);
                }
            }

            throw new RuntimeException("The height was not in range for anything.");
        }

        public static ColorGradient createTerrainStyleGradient() {
            return new ColorGradient(
                    new SingularColorGradient(Double.NEGATIVE_INFINITY, 0, Color.BLACK, Color.BLACK),
                    new SingularColorGradient(0, 0.3, Color.BLACK, Color.BLUE),
                    new SingularColorGradient(0.3, 0.35, Color.YELLOW, Color.YELLOW),
                    new SingularColorGradient(0.35, 0.4, Color.YELLOW, Color.GREEN),
                    new SingularColorGradient(0.4, 0.6, Color.GREEN, Color.GREEN),
                    new SingularColorGradient(0.6, 0.8, Color.DARK_GRAY, Color.GRAY),
                    new SingularColorGradient(0.8, 1, Color.LIGHT_GRAY, Color.WHITE),
                    new SingularColorGradient(1, Double.POSITIVE_INFINITY, Color.WHITE, Color.WHITE));
        }

        public static DoubleToIntFunction createStripedGradient(double changeFrequency) {
            return new DoubleToIntFunction() {
                @Override
                public int applyAsInt(double value) {
                    if (value % changeFrequency < changeFrequency / 2.0) {
                        return 0x4F000000;
                    } else {
                        return 0x4FFFFFFF;
                    }
                }
            };
        }

        @Override
        public int applyAsInt(double value) {
            return getColor(value);
        }
    }

    public static class PixelGrid implements Shape {
        private final int x, y, displayWidth, displayHeight;
        private final int[][] rgbValues;

        public static int coloredRGBGenerator(double height) {
            if (height < 0) {
                return Color.BLACK.getRGB();
            } else if (height < 0.3) {
                return Color.BLUE.getRGB();
            } else if (height < 0.35) {
                return Color.YELLOW.getRGB();
            } else if (height < 0.6) {
                return Color.GREEN.getRGB();
            } else if (height < 0.8) {
                return Color.GRAY.getRGB();
            } else {
                return Color.WHITE.getRGB();
            }
        }

        public PixelGrid(double[][] heightMap, int x, int y, int displayWidth, int displayHeight,
                DoubleToIntFunction heightToRGBFunction) {
            if (heightToRGBFunction == null) {
                heightToRGBFunction = (height) -> Color.HSBtoRGB(0, 0,
                        (float) Math.max(0, Math.min(1, height)));
            }

            this.rgbValues = new int[heightMap.length][heightMap[0].length];
            for (int row = 0; row < heightMap.length; row++) {
                for (int col = 0; col < heightMap[0].length; col++) {
                    this.rgbValues[row][col] = heightToRGBFunction.applyAsInt(heightMap[row][col]);
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
                yPoints[i] = (int) Math
                        .floor(MathUtils.mapToRange(heightMap[i], heightMapMin, heightMapMax, 0, viewHeight));
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

    public static class ColorMap implements Shape {
        @FunctionalInterface
        public static interface TwoDimensionToOneDimensionFunction {
            public double apply(double x, double y);
        }

        private final int displayWidth, displayHeight;
        private final double viewXStart,
                viewXEnd, viewYStart, viewYEnd;
        private TwoDimensionToOneDimensionFunction function;
        private DoubleToIntFunction heightToRGBFunction;

        public ColorMap(TwoDimensionToOneDimensionFunction function, int displayWidth, int displayHeight,
                double viewXStart, double viewXEnd, double viewYStart, double viewYEnd,
                DoubleToIntFunction heightToRGBFunction) {
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
            this.viewXStart = viewXStart;
            this.viewXEnd = viewXEnd;
            this.viewYStart = viewYStart;
            this.viewYEnd = viewYEnd;
            this.function = function;
            this.heightToRGBFunction = heightToRGBFunction;
        }

        @Override
        public void paintOnTo(Graphics g) {
            double[][] heightMap = new double[displayHeight][displayWidth];

            for (int y = 0; y < heightMap.length; y++) {
                for (int x = 0; x < heightMap[0].length; x++) {
                    heightMap[y][x] = function.apply(
                            MathUtils.mapToRange(x, 0, heightMap[0].length, viewXStart, viewXEnd),
                            MathUtils.mapToRange(y, 0, heightMap.length, viewYEnd, viewYStart));
                }
            }

            new PixelGrid(heightMap, 0, 0, displayHeight, displayWidth, heightToRGBFunction).paintOnTo(g);
        }
    }

    public static class Graph implements Shape {
        private int displayWidth, displayHeight;
        private double viewXStart, viewXEnd, viewYStart, viewYEnd;
        private ArrayList<DoubleUnaryOperator> functions;

        public Graph(int displayWidth, int displayHeight, double viewXStart,
                double viewXEnd, double viewYStart, double viewYEnd) {
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
            this.viewXStart = viewXStart;
            this.viewXEnd = viewXEnd;
            this.viewYStart = viewYStart;
            this.viewYEnd = viewYEnd;
            this.functions = new ArrayList<>();
        }

        public void addFunction(DoubleUnaryOperator function) {
            functions.add(function);
        }

        @Override
        public void paintOnTo(Graphics g) {
            for (DoubleUnaryOperator function : functions) {
                int prevX = -1;
                double prevY = Double.NaN;

                for (int i = 1; i <= displayWidth; i++) {
                    int newX = i;
                    double newY = MathUtils.mapToRange(function.applyAsDouble(
                            MathUtils.mapToRange(newX, 0, displayWidth, viewXStart, viewXEnd)),
                            viewYStart, viewYEnd, displayHeight, 0);

                    if (!Double.isNaN(prevY) && !Double.isNaN(newY)) {
                        g.drawLine(prevX, (int) prevY, newX, (int) newY);
                    }

                    prevX = newX;
                    prevY = newY;
                }
            }

            // - x-axis
            int viewY0ToDisplayY = (int) MathUtils.mapToRange(0, viewYStart, viewYEnd, displayHeight, 0);
            g.drawLine(0, viewY0ToDisplayY, displayWidth, viewY0ToDisplayY);

            // | y-axis
            int viewX0ToDisplayX = (int) MathUtils.mapToRange(0, viewXStart, viewXEnd, 0, displayWidth);
            g.drawLine(viewX0ToDisplayX, 0, viewX0ToDisplayX, displayHeight);
        }
    }

    public static class VectorField implements Shape {
        private final int displayWidth, displayHeight;
        private final double viewXStart, viewXEnd, viewYStart, viewYEnd;

        // A vector is in the form of: {startX, startY, width, height}
        private double[][] vectors;

        public VectorField(int displayWidth, int displayHeight, double viewXStart, double viewXEnd, double viewYStart,
                double viewYEnd, double[][] vectors) {
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
            this.viewXStart = viewXStart;
            this.viewXEnd = viewXEnd;
            this.viewYStart = viewYStart;
            this.viewYEnd = viewYEnd;

            this.vectors = vectors;
        }

        @Override
        public void paintOnTo(Graphics g) {
            for (double[] vector : vectors) {
                // vector: {x, y, width, height}
                int x1 = (int) mapToRange(vector[0], viewXStart, viewXEnd, 0, displayWidth),
                        y1 = (int) mapToRange(vector[1], viewYStart, viewYEnd, displayHeight, 0),
                        x2 = (int) mapToRange(vector[0] + vector[2], viewXStart, viewXEnd, 0, displayWidth),
                        y2 = (int) mapToRange(vector[1] + vector[3], viewYStart, viewYEnd, displayHeight, 0);

                g.fillOval(x1 - 2, y1 - 2, 4, 4);
                g.drawLine(x1, y1, x2, y2);
            }
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
