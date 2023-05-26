package simple;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.function.DoubleUnaryOperator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import simple.Canvas.Graph;

public class Perlin {
    /**
     * Look inside the code for the math
     * 
     * @param value0 p(0)
     * @param value1 p(1)
     * @param slope0 p'(0)
     * @param slope1 p'(1)
     * @return p a cubic function
     */
    private static DoubleUnaryOperator createCubic(double value0, double value1, double slope0, double slope1) {
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
    private static DoubleUnaryOperator createCubic(double slope0, double slope1) {
        return (height) -> (slope0 + slope1) * height * height * height
                + (-2 * slope0 - slope1) * height * height + (slope0) * height;
    }

    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("Midpoint Displayment Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(540, 540));

        JPanel controlPanel = new JPanel();
        {
            final int SCALE_FACTOR = 10;

            JSlider slope0Slider = new JSlider(-2 * SCALE_FACTOR, 2 * SCALE_FACTOR);
            JSlider slope1Slider = new JSlider(-2 * SCALE_FACTOR, 2 * SCALE_FACTOR);

            ChangeListener slopeChangeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    canvas.clear();

                    Graph graph = new Graph(540, 540, -0.5, 1.5, -1, 1);

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
}
