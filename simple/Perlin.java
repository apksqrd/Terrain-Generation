package simple;

import static simple.MathUtils.*;
import static simple.MathUtils.clamp;
import static simple.MathUtils.random;
import static simple.MathUtils.sum;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongToDoubleFunction;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import simple.Canvas.*;
import simple.Canvas.ColorMap;
import simple.Canvas.Graph;

class Perlin1D {
    /**
     * Look inside the code for the math
     * 
     * @param value0 p(0)
     * @param value1 p(1)
     * @param slope0 p'(0)
     * @param slope1 p'(1)
     * @return p a cubic function
     */
    public static DoubleUnaryOperator createCubic(double value0, double value1, double slope0, double slope1) {
        /*
         * let: p(x) = n3 * x^3 + n2 * x^2 + n1 * x^1 + n0 * x^0
         * 
         * p(x) = n3 * x^3 + n2 * x^2 + n1 * x + n0
         * p'(x) = 3 * n3 * x^2 + 2 * n2 * x + n1
         * 
         * value0:
         * value0 = p(0) = 0 + 0 + 0 + n0
         * -> n0 = value0
         * 
         * slope0:
         * slope0 = p'(0) = n1
         * -> n1 = slope0
         * 
         * value1:
         * value1 = p(1) = n3 + n2 + n1 + n0
         * -> n3 + n2 + n1 + n0 = value1
         * -> n3 + n2 + slope0 + value0 = value1
         * -> n3 + n2 = value1 - slope0 - value0
         * 
         * slope1:
         * slope1 = p'(1) = 3 * n3 + 2 * n2 + n1
         * -> 3 * n3 + 2 * n2 + n1 = slope1
         * -> 3 * n3 + 2 * n2 + slope0 = slope1
         * -> 3 * n3 + 2 * n2 = slope1 - slope0
         * 
         * left with:
         * n0 = value0
         * n1 = slope0
         * n3 + n2 = value1 - slope0 - value0
         * 3 * n3 + 2 * n2 = slope1 - slope0
         * 
         * n3 + n2 = value1 - slope0 - value0
         * -> n3 = value1 - slope0 - value0 - n2
         * 3 * n3 + 2 * n2 = slope1 - slope0
         * -> 3 * (value1 - slope0 - value0 - n2) + 2 * n2 = slope1 - slope0
         * -> 3 * value1 - 3 * slope0 - 3 * value0 - 3 * n2 + 2 * n2 = slope1 - slope0
         * -> 3 * value1 - 3 * slope0 - 3 * value0 - n2 = slope1 - slope0
         * -> n2 = 3 * value1 - 3 * slope0 - 3 * value0 - slope1 + slope0
         * -> n2 = 3 * value1 - 2 * slope0 - 3 * value0 - slope1
         * n3 = value1 - slope0 - value0 - n2
         * -> n3 = value1 - slope0 - value0 - (3value1 - 2slope0 - 3value0 - slope1)
         * -> n3 = value1 - slope0 - value0 - 3value1 + 2slope0 + 3value0 + slope1
         * -> n3 = slope0 + slope1 + 2 * value0 - 2 * value1
         * 
         */
        return (height) -> (slope0 + slope1 + 2 * value0 - 2 * value1) * height * height * height
                + (3 * value1 - 2 * slope0 - 3 * value0 - slope1) * height * height + (slope0) * height + value0;
    }

    // assumes p(0) = p(1) = 0
    public static DoubleUnaryOperator createCubic(double slope0, double slope1) {
        return (height) -> (slope0 + slope1) * height * height * height
                + (-2 * slope0 - slope1) * height * height + (slope0) * height;
    }

    public static double cubicSlopeInterpolation(double x, double slope0, double slope1) {
        return (slope0 + slope1) * x * x * x + (-2 * slope0 - slope1) * x * x + (slope0) * x;
    }

    /**
     * Assumes x is non-negative.
     * 
     * The range of perlin is smaller than the range of the slopes.
     * 
     * @param x
     * @param slopeGenerator
     * @return
     */
    public static double cubicPerlin(double x, LongToDoubleFunction slopeGenerator) {
        return cubicSlopeInterpolation(x % 1, slopeGenerator.applyAsDouble((long) x),
                slopeGenerator.applyAsDouble((long) x + 1));
    }

    public static void cubicSlopeInterpolationDemonstration() {
        JFrame mainFrame = new JFrame("Cubic Slope Interpolation Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(540, 540));

        JPanel controlPanel = new JPanel();

        {
            final int SCALE_FACTOR = 10;

            JSlider slope0Slider = new JSlider(-4 * SCALE_FACTOR, 4 * SCALE_FACTOR);
            JSlider slope1Slider = new JSlider(-4 * SCALE_FACTOR, 4 * SCALE_FACTOR);
            slope0Slider.setValue(1 * SCALE_FACTOR);
            slope1Slider.setValue(1 * SCALE_FACTOR);

            ChangeListener slopeChangeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    canvas.clear();

                    Graph graph = new Graph(540, 540, -0.5, 1.5, -1.5, 1.5);

                    double slope0 = (double) slope0Slider.getValue() / SCALE_FACTOR;
                    double slope1 = (double) slope1Slider.getValue() / SCALE_FACTOR;

                    graph.addFunction(createCubic(slope0, slope1));
                    graph.addFunction((x) -> slope0 * x);
                    graph.addFunction((x) -> slope1 * (x - 1));

                    canvas.addShape(graph);
                }
            };

            slope0Slider.addChangeListener(slopeChangeListener);
            slope1Slider.addChangeListener(slopeChangeListener);

            controlPanel.add(slope0Slider);
            controlPanel.add(slope1Slider);

            slopeChangeListener.stateChanged(null);
        }

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.add(controlPanel, BorderLayout.EAST);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static void cubicPerlinDemo() {
        JFrame mainFrame = new JFrame("Cubic Perlin Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(540, 540));

        JPanel controlPanel = new JPanel();

        {
            ChangeListener slopeChangeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    canvas.clear();

                    Graph graph = new Graph(540, 540, -0.5, 29.5, -1.5, 1.5);

                    graph.addFunction((x) -> -1);
                    graph.addFunction((x) -> 1);
                    graph.addFunction((x) -> cubicPerlin(x, (seed) -> random(seed, -4, 4)));

                    canvas.addShape(graph);
                }
            };

            slopeChangeListener.stateChanged(null);
        }

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.add(controlPanel, BorderLayout.EAST);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static double fadeSlopeInterpolation(double x, double slope0, double slope1,
            DoubleUnaryOperator fadeFunction) {
        double yFromLine0 = slope0 * x, yFromLine1 = slope1 * (x - 1);

        return fadeInterpolation(x, yFromLine0, yFromLine1, fadeFunction);
        // this actually makes sense
    }

    public static double fadeInterpolation(double x, double point0, double point1, DoubleUnaryOperator fadeFunction) {
        double weight0 = 1 - fadeFunction.applyAsDouble(x), weight1 = fadeFunction.applyAsDouble(x);
        return weight0 * point0 + weight1 * point1;
    }

    public static double fadePerlin(double x, LongToDoubleFunction slopeGenerator, DoubleUnaryOperator fadeFunction) {
        return fadeSlopeInterpolation(x % 1, slopeGenerator.applyAsDouble((long) x),
                slopeGenerator.applyAsDouble((long) x + 1), fadeFunction);
    }

    public static void fadeFunctionInterpolationDemo() {
        // i am pretty sure this is the more proper way of doing it
        JFrame mainFrame = new JFrame("Fade Slope Interpolation Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(540, 540));

        JPanel controlPanel = new JPanel();

        {
            JComboBox<String> fadeFunctionComboBox = new JComboBox<String>(
                    new String[] { "Smoothstep", "Smootherstep", "Lerp" });

            // TODO: add clamp option

            final int SCALE_FACTOR = 10;

            JSlider slope0Slider = new JSlider(-4 * SCALE_FACTOR, 4 * SCALE_FACTOR);
            JSlider slope1Slider = new JSlider(-4 * SCALE_FACTOR, 4 * SCALE_FACTOR);
            slope0Slider.setValue(1 * SCALE_FACTOR);
            slope1Slider.setValue(1 * SCALE_FACTOR);

            ChangeListener slopeChangeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    canvas.clear();

                    Graph graph = new Graph(540, 540, -0.5, 1.5, -1.5, 1.5);

                    // DoubleUnaryOperator fadeFunction = (y) -> y * y * y * (y * (6.0 * y - 15.0) +
                    // 10.0);
                    DoubleUnaryOperator fadeFunction = switch ((String) fadeFunctionComboBox.getSelectedItem()) {
                        case "Smoothstep" -> x -> x * x * (3.0 - 2.0 * x);
                        case "Smootherstep" -> x -> x * x * x * (x * (6.0 * x - 15.0) + 10.0);
                        case "Lerp" -> x -> x;
                        default -> null;
                    };

                    double slope0 = (double) slope0Slider.getValue() / SCALE_FACTOR;
                    double slope1 = (double) slope1Slider.getValue() / SCALE_FACTOR;

                    // graph.addFunction(fadeFunction);
                    graph.addFunction((x) -> fadeSlopeInterpolation(x, slope0, slope1, fadeFunction));
                    graph.addFunction((x) -> slope0 * x);
                    graph.addFunction((x) -> slope1 * (x - 1));

                    canvas.addShape(graph);
                }
            };

            slope0Slider.addChangeListener(slopeChangeListener);
            slope1Slider.addChangeListener(slopeChangeListener);
            fadeFunctionComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    slopeChangeListener.stateChanged(null);
                };
            });

            controlPanel.add(fadeFunctionComboBox);
            controlPanel.add(slope0Slider);
            controlPanel.add(slope1Slider);

            slopeChangeListener.stateChanged(null);
        }

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.add(controlPanel, BorderLayout.EAST);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static void fadePerlinDemo() {
        // i am pretty sure this is the more proper way of doing it
        JFrame mainFrame = new JFrame("Perlin Noise Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(540, 540));

        JPanel controlPanel = new JPanel();

        {
            JComboBox<String> fadeFunctionComboBox = new JComboBox<String>(
                    new String[] { "Smoothstep", "Smootherstep", "Lerp" });

            ActionListener fadeFunctionChangedListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    canvas.clear();

                    Graph graph = new Graph(540, 540, -0.5, 29.5, -1.5, 1.5);

                    // DoubleUnaryOperator fadeFunction = (y) -> y * y * y * (y * (6.0 * y - 15.0) +
                    // 10.0);
                    DoubleUnaryOperator fadeFunction = switch ((String) fadeFunctionComboBox.getSelectedItem()) {
                        case "Smoothstep" -> x -> x * x * (3.0 - 2.0 * x);
                        case "Smootherstep" -> x -> x * x * x * (x * (6.0 * x - 15.0) + 10.0);
                        case "Lerp" -> x -> x;
                        default -> null;
                    };

                    // graph.addFunction(fadeFunction);
                    graph.addFunction((x) -> -1);
                    graph.addFunction((x) -> 1);
                    graph.addFunction((x) -> fadePerlin(x, (seed) -> random(seed, -2, 2), fadeFunction));

                    // show slopes
                    graph.addFunction((x) -> fadePerlin(x, (seed) -> random(seed, -2, 2), p -> {
                        double remainder = p % 1;
                        if (0.4 < remainder && remainder < 0.6) {
                            // to disconnect
                            return Double.NaN;
                        } else
                            return Math.round(p);
                    }));

                    canvas.addShape(graph);
                }
            };

            fadeFunctionComboBox.addActionListener(fadeFunctionChangedListener);

            controlPanel.add(fadeFunctionComboBox);

            fadeFunctionChangedListener.actionPerformed(null);
        }

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.add(controlPanel, BorderLayout.EAST);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        fadeFunctionInterpolationDemo();
    }
}

public class Perlin {
    /**
     * Math to ensure that the slopes will match. Right now, it isn't and idk if it
     * is bc weight function or interpolation.
     * 
     * In one dimension (I will just do math for 1D and assume rest works).
     * 
     * 
     * Reminder: point slope form: y = m*(x-x1)
     * equation0(x) = slope0*(x - 0)
     * equation1(x) = slope1*(x - 1)
     * 
     * offset(a, b) = abs(a - b) // for higher dimensions, it doesn't return
     * distance. it returns an array of double for each dimension's offset
     * 
     * slopeInterpolation(x, slope0, slope1) =
     * = equation0(x) * weight(offset(x, 0)) + equation1(x) * weight(offset(x, 1))
     * = slope0*x*weight(x) + slope1*(x-1)*weight(1-x)
     * 
     * I am going to abbreviate from now.
     * 
     * sI(x) =
     * = s0*x*w(x) + s1*(x-1)*w(1-x)
     * 
     * slopeInterpolation'(x) =
     * = s0*w(x) + s0*x*w'(x) + s1*w(1-x) - s1*(x-1)*w'(1-x)
     * 
     * Find weight(offset) so that sI(0)=0 sI(1)=0 sI'(0)=slope0 sI'(1)=slope1
     * 
     * 0 = sI(0) = s0*0*w(0) + s1*(-1)*w(1)
     * w(1) = 0
     * 
     * 0 = sI(1) = s0*1*w(1) + s1*0*w(1)
     * w(1) = 0
     * 
     * s0 = sI'(0)
     * = s0*w(0) + s0*0*w'(0) + s1*w(1) - s1*(-1)*w'(1)
     * = s0*w(0) + 0 + 0 + s1*w'(1)
     * One possibility is w(0) = 1 and w'(1) = 0
     * 
     * s1 = sI'(1)
     * = s0*w(1) + s0*1*w'(1) + s1*w(0) - s1*0*w'(0)
     * = 0 + s0*w'(1) + s1*w(0) - 0
     * Works with previous possibility
     * 
     * So, the simplest w(os) woule have w(0) = 1, w(1) = 0, w'(1) = 0.
     * 
     * wV0(os) = (x - 1)^2
     * wV0'(os) = 2*(x-1)
     * 
     * For the current implementation, I did something slightly different, but
     * similar. However, the clamping is applied at a unit circle range. I wonder if
     * the clamped area can be decreased. The edges still have to be clamped, but
     * maybe if instead of thinking about the problem with distance and I instead
     * took advantage of the fact that there is a square... The thing from 1/x is
     * closer to being a square than a circle. It can also be x^-n where the higher
     * n is, the more square it gets. I hope that there is a function where the
     * clamped area is 0 units^2. Manhattan distance doesn't work because if there
     * is so much clamp that there is a point (usually center) where all corners
     * have no weight, then that is obviously a problem. Applying a smooth function
     * to the biggest coordinate could also work...
     * 
     * The two I have right now all work, but they are weird. For example, when
     * every slope faces up, then the height is not the same left and right. To fix
     * this, I could somehow get the weighted average slope and then find the height
     * from that slope.
     * 
     * * Cool observation:
     ***** A nice side effect of the weight interpolation system is that in the input
     ***** coordinate of where the two equations have same outcomes, the interpolated
     ***** equation has the same value because the weighted average of a bunch of the
     ***** same value will be that value.
     */
    public static double sumCoordinateSquaredWeightCalculator(double... offsets) {

        double weirdThing = sum(apply(offsets, x -> x * x));
        // like distance w/out the sqrt

        double temp = clamp(weirdThing) - 1;
        return temp * temp;
    }

    public static double maxCoordinateWeightCalculator(DoubleUnaryOperator singleDimensionalWeightCalculator,
            double... offsets) {
        double maxCoordinate = max(offsets);
        return singleDimensionalWeightCalculator.applyAsDouble(maxCoordinate);
    }

    public static double parabolic1DWeightCalculator(double offset) {
        return clamp((offset - 1) * (offset - 1));
    }

    public static double[] defaultSlopeGenerator(long... coords) {
        long[] temp = new long[coords.length + 1];
        System.arraycopy(coords, 0, temp, 0, coords.length);

        double[] slope = new double[coords.length];

        for (int dimension = 0; dimension < coords.length; dimension++) {
            temp[temp.length - 1] = dimension; // or else, all the slopes will be the same.
            slope[dimension] = random(-2, 2, temp);
        }

        return slope;
    };

    @FunctionalInterface
    public static interface MultiInputDoubleFunction {
        public double apply(double... inputs);
    }

    @FunctionalInterface
    public static interface MultiLongToMultiDoubleFunction {
        public double[] apply(long... inputs);
    }

    /**
     * The points in data have one more dimension than input.
     * 
     * @param input
     * @param data
     * @param weightCalculator
     * @return
     */
    public static double interpolate(double[] input, double[][] data, MultiInputDoubleFunction weightCalculator) {
        double weightedSum = 0, sumOfWeights = 0;

        for (double[] datum : data) {
            double[] differences = new double[input.length];

            for (int dimension = 0; dimension < input.length; dimension++) {
                differences[dimension] = Math.abs(input[dimension] - datum[dimension]);
            }

            double weight = weightCalculator.apply(differences), weightedValue = weight * datum[datum.length - 1];
            weightedSum += weightedValue;
            sumOfWeights += weight;
        }

        return weightedSum / sumOfWeights;
    }

    public static void interpolationDemo2D() {
        JFrame mainFrame = new JFrame("2D Value Interpolation Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(540, 540));

        JPanel controlPanel = new JPanel();

        {
            final int SCALE_FACTOR = 10;

            JSlider point00Slider = new JSlider(0 * SCALE_FACTOR, 1 * SCALE_FACTOR);
            JSlider point01Slider = new JSlider(0 * SCALE_FACTOR, 1 * SCALE_FACTOR);
            JSlider point10Slider = new JSlider(0 * SCALE_FACTOR, 1 * SCALE_FACTOR);
            JSlider point11Slider = new JSlider(0 * SCALE_FACTOR, 1 * SCALE_FACTOR);
            point00Slider.setValue(1 * SCALE_FACTOR);
            point01Slider.setValue(1 * SCALE_FACTOR);
            point10Slider.setValue(1 * SCALE_FACTOR);
            point11Slider.setValue(1 * SCALE_FACTOR);

            ChangeListener slopeChangeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    canvas.clear();

                    double point00 = (double) point00Slider.getValue() / SCALE_FACTOR;
                    double point01 = (double) point01Slider.getValue() / SCALE_FACTOR;
                    double point10 = (double) point10Slider.getValue() / SCALE_FACTOR;
                    double point11 = (double) point11Slider.getValue() / SCALE_FACTOR;

                    double[][] data = new double[][] {
                            { 0, 0, point00 },
                            { 0, 1, point01 },
                            { 1, 0, point10 },
                            { 1, 1, point11 }
                    };

                    canvas.addShape(new ColorMap(((x, y) -> interpolate(new double[] { x, y }, data, (differences) -> {
                        double distance = differences[0] * differences[0] + differences[1] *
                                differences[1];
                        // System.out.println(distance);
                        return Math.max(0, Math.min(1, 1 - distance));
                    })), 540, 540,
                            0, 1, 0, 1, null));
                }
            };

            point00Slider.addChangeListener(slopeChangeListener);
            point01Slider.addChangeListener(slopeChangeListener);
            point10Slider.addChangeListener(slopeChangeListener);
            point11Slider.addChangeListener(slopeChangeListener);

            controlPanel.add(point00Slider);
            controlPanel.add(point01Slider);
            controlPanel.add(point10Slider);
            controlPanel.add(point11Slider);

            slopeChangeListener.stateChanged(null);
        }

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.add(controlPanel, BorderLayout.EAST);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    /**
     * Assume the points for slopes are corners with coords 0 or 1.
     * 
     * @param point
     * @param slopes
     * @param weightCalculator
     * @return
     */
    public static double slopeInterpolation(double[] point,
            double[][] slopes, MultiInputDoubleFunction weightCalculator) {
        /*
         * Ideas for interpolation (in context of 2 input dimensions)
         * 
         * To interpolate four slopes at different points, I can create four planes for
         * each slope. Then, for each point in the input, I can return an output by
         * taking some weighted average of each plane's output at that point.
         * 
         * This is easy in 1D. I just really need one weight, and the other weight is 1
         * minus the weight. One caviat is that ideally, all the weights add up to one
         * to simplify calculation. Additionally, the weights should be 1 when distance
         * is 0 and the weight should be 0 when the distance is furthest possible, and
         * this isn't required but it makes sense to never make weight increase as
         * distance increases. Finally, the dweight/ddist should be 0 at both extremes
         * for distance. (This is because of the product rule and at the extremes, we
         * want the weighted averages to have the same slope as originally. I won't do
         * the math here because I didn't, but I am guessing that is why Lerp doesn't
         * work.)
         * 
         * 2D is going to be similar but with more rules. The first difference I
         * realized is that if the point was on an edge of the square, we need to make
         * sure only the weight of the two vertices of the edge are considered. The two
         * far vertices should be considered 0 so that the transition between one box
         * and the other seems smooth. This means that having a weight function with one
         * input, the distance between the desired gridpoint and point would require
         * clamping, which I don't like the sound of. So, I will make it take in two
         * inputs: deltaX and deltaY. This will also allow for a more flexible function.
         * For now, I won't worry about having it all add up to one because that is just
         * a preference for elegance on my part.
         * 
         * I was just doing a demo for interpolation without the slope, and in that I
         * used weight = 1 - the sum of the squares of each distance for each square.
         * Surprisingly, this seems to have a derivative of 0 at the corners.
         */
        {
            // check dimensions
            // assume slopes is rectangular
            assert point.length == slopes[0].length;
            assert slopes.length == 1 << point.length;
        }

        // the last element of a datum is the height that point would be at according to
        // the plane
        // the other elements are the coordinates for the corner in which that plane was
        // based off of
        // the output dimension in a data[i] represents the height of the point
        // according to a plane that is 0 at the input coordinates and has a slope of
        // slopes[i] the actual output coordinate of the point used to calculate this is
        // 0
        double[][] data = new double[slopes.length][point.length + 1];

        for (int i = 0; i < slopes.length; i++) {
            double[] slope = slopes[i];
            double[] datum = data[i];

            // give coords offset for plane
            for (int dimension = 0; dimension < point.length; dimension++) {
                datum[dimension] = (i >> dimension) & 1; // either 0 or 1
            }

            // find the equation from coordinate and slopes
            // use point slope form where y = m0(x0-p0) + m1(x1-p1) ... + b
            // mn is slope in the nth dimension
            // xn is the input for the plane
            // pn is the coordinate for the point we know it passes through
            // and b is the output at that point. In this case the ms are from slope,
            // xs are from point, and ps are from the first few datum. b is 0.
            double height = 0;
            for (int dimension = 0; dimension < point.length; dimension++) {
                height += slope[dimension] * (point[dimension] - datum[dimension]);
            }

            datum[datum.length - 1] = height;
        }

        // System.out.println(Arrays.deepToString(data));

        return interpolate(point, data, weightCalculator);
    }

    public static double perlin(double[] point, MultiLongToMultiDoubleFunction slopeCalculator,
            MultiInputDoubleFunction weightCalculator) {
        double[][] slopes = new double[1 << point.length][point.length];
        {
            long[] flooredPoint = Arrays.stream(point).mapToLong(x -> (long) x).toArray();

            for (int i = 0; i < slopes.length; i++) {
                long[] cornerCoords = flooredPoint.clone();
                for (int dimension = 0; dimension < cornerCoords.length; dimension++) {
                    cornerCoords[dimension] += (i >> dimension) & 1;
                }

                slopes[i] = slopeCalculator.apply(cornerCoords);
            }
        }

        double[] offset = apply(point, x -> x % 1);

        return slopeInterpolation(offset, slopes, weightCalculator);
    }

    public static double perlin(double... point) {
        return perlin(point, Perlin::defaultSlopeGenerator, Perlin::sumCoordinateSquaredWeightCalculator);
    }

    public static void slopeInterpolationDemo1D() {
        JFrame mainFrame = new JFrame("Weighted Slope Interpolation Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(540, 540));

        JPanel controlPanel = new JPanel();

        {
            // TODO: add clamp option

            final int SCALE_FACTOR = 10;

            JSlider slope0Slider = new JSlider(-4 * SCALE_FACTOR, 4 * SCALE_FACTOR);
            JSlider slope1Slider = new JSlider(-4 * SCALE_FACTOR, 4 * SCALE_FACTOR);
            slope0Slider.setValue(1 * SCALE_FACTOR);
            slope1Slider.setValue(1 * SCALE_FACTOR);

            ChangeListener slopeChangeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    canvas.clear();

                    Graph graph = new Graph(540, 540, -0.5, 1.5, -1.5, 1.5);

                    double slope0 = (double) slope0Slider.getValue() / SCALE_FACTOR;
                    double slope1 = (double) slope1Slider.getValue() / SCALE_FACTOR;

                    // graph.addFunction(fadeFunction);
                    graph.addFunction((x) -> slopeInterpolation(new double[] { x },
                            new double[][] { { slope0 }, { slope1 } }, Perlin::sumCoordinateSquaredWeightCalculator));
                    // graph.addFunction((x) -> slopeInterpolation(new double[] { x },
                    // new double[][] { { slope0 }, { slope1 } }, fadeFunction));
                    graph.addFunction((x) -> slope0 * x);
                    graph.addFunction((x) -> slope1 * (x - 1));

                    canvas.addShape(graph);
                }
            };

            slope0Slider.addChangeListener(slopeChangeListener);
            slope1Slider.addChangeListener(slopeChangeListener);

            controlPanel.add(slope0Slider);
            controlPanel.add(slope1Slider);

            slopeChangeListener.stateChanged(null);
        }

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.add(controlPanel, BorderLayout.EAST);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static void perlinNoiseDemo1D() {
        JFrame mainFrame = new JFrame("1D Perlin Noise Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(540, 540));

        JPanel controlPanel = new JPanel();

        {
            canvas.clear();

            Graph graph = new Graph(540, 540, -0.5, 29.5, -1.5, 1.5);

            // graph.addFunction(fadeFunction);
            graph.addFunction((x) -> -1);
            graph.addFunction((x) -> 1);
            graph.addFunction((x) -> perlin(x));

            // show slopes
            graph.addFunction(
                    (x) -> perlin(new double[] { x }, Perlin::defaultSlopeGenerator,
                            offset -> offset[0] < 0.5 ? 1 : 0));

            canvas.addShape(graph);
        }

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.add(controlPanel, BorderLayout.EAST);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static void perlinNoiseDemo2D() {
        JFrame mainFrame = new JFrame("Fade Slope Interpolation Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(540, 540));

        JPanel controlPanel = new JPanel();

        {
            JComboBox<String> weightFunctionComboBox = new JComboBox<String>(
                    new String[] { "Sum Coord Squared", "Max Coord" });

            JComboBox<String> colorGradientComboBox = new JComboBox<String>(
                    new String[] { "Default", "Stripes", "Game Terrain" });

            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    canvas.clear();

                    int VIEW_SIZE = 5;

                    MultiLongToMultiDoubleFunction slopeGenerator = Perlin::defaultSlopeGenerator;
                    MultiInputDoubleFunction weightCalculator = switch ((String) weightFunctionComboBox
                            .getSelectedItem()) {
                        case "Sum Coord Squared" -> Perlin::sumCoordinateSquaredWeightCalculator;
                        case "Max Coord" ->
                            offsets -> maxCoordinateWeightCalculator(Perlin::parabolic1DWeightCalculator, offsets);
                        default -> null;
                    };
                    DoubleToIntFunction colorGradient = switch ((String) colorGradientComboBox.getSelectedItem()) {
                        case "Default" -> null;
                        case "Stripes" -> ColorGradient.createStripedGradient(0.01);
                        case "Game Terrain" -> ColorGradient.createTerrainStyleGradient();
                        default -> null;
                    };

                    canvas.addShape(new ColorMap(
                            (x, y) -> MathUtils.mapToRange(
                                    perlin(new double[] { x, y }, slopeGenerator,
                                            weightCalculator),
                                    -1, 1, 0, 1),
                            540, 540, 0, VIEW_SIZE, 0, VIEW_SIZE, colorGradient));

                    // display the slopes
                    double SLOPE_SCALE = 0.5;
                    double[][] slopes = new double[(VIEW_SIZE + 1) * (VIEW_SIZE + 1)][4];
                    for (int i = 0; i < VIEW_SIZE + 1; i++) {
                        for (int j = 0; j < VIEW_SIZE + 1; j++) {
                            slopes[VIEW_SIZE * i + j][0] = i;
                            slopes[VIEW_SIZE * i + j][1] = j;

                            double[] slope = slopeGenerator.apply(i, j);
                            slopes[VIEW_SIZE * i + j][2] = SLOPE_SCALE * slope[0];
                            slopes[VIEW_SIZE * i + j][3] = SLOPE_SCALE * slope[1];
                        }
                    }

                    canvas.addShape(new VectorField(540, 540, 0, VIEW_SIZE, 0, VIEW_SIZE, slopes));
                }
            };

            actionListener.actionPerformed(null);

            weightFunctionComboBox.addActionListener(actionListener);
            colorGradientComboBox.addActionListener(actionListener);

            controlPanel.add(weightFunctionComboBox);
            controlPanel.add(colorGradientComboBox);
        }

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.add(controlPanel, BorderLayout.EAST);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        interpolationDemo2D();

        // System.out.println(
        // slopeInterpolation(new double[] { 0.5, 0.6 }, new double[][] { { 0, 1 }, { 0,
        // 1 }, { 0, 1 }, { 0, 1 } },
        // Perlin::defaultWeightCalculator));
    }
}