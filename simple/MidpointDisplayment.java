package simple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import simple.Canvas.Polygon;

public class MidpointDisplayment {
    public static double[] doIteration(double[] initialHeights, double mutationRate,
            boolean isMutationRateMultiplicative) {
        double[] nextHeights = new double[2 * initialHeights.length - 1];

        for (int i = 0; i < initialHeights.length - 1; i++) {
            nextHeights[2 * i] = initialHeights[i];
            if (isMutationRateMultiplicative) {
                nextHeights[2 * i + 1] = Canvas.mapToRange((2 * Math.random() - 1) * mutationRate, -1, 1,
                        initialHeights[i],
                        initialHeights[i + 1]);
            } else {
                double midpoint = (initialHeights[i] + initialHeights[i + 1]) / 2.0;
                nextHeights[2 * i + 1] = Canvas.mapToRange(Math.random(), 0, 1, midpoint - mutationRate,
                        midpoint + mutationRate);
            }
        }
        nextHeights[nextHeights.length - 1] = initialHeights[initialHeights.length - 1];

        return nextHeights;
    }

    public static void displayOneDimensionalHeightMap(double[] heightMap, int viewWidth, int viewHeight,
            int heightMapMin, int heightMapMax) {
        JFrame mainFrame = new JFrame("Display");
        mainFrame.setSize(viewWidth, viewHeight);

        Canvas canvas = new Canvas();

        canvas.addShape(new Polygon(heightMap, viewWidth, viewHeight, heightMapMin, heightMapMax));

        mainFrame.add(canvas);

        mainFrame.setVisible(true);
    }

    private static final int DEFAULT_WIDTH = 540, DEFAULT_HEIGHT = 360;

    public static double[] defaultGenerateHeightmap(int iterations, IntToDoubleFunction mutationRateGenerator,
            boolean isMutationRateMultiplicative) {
        double[] heightMap = new double[] { Math.random(), Math.random() };

        for (int i = 0; i < iterations; i++) {
            heightMap = doIteration(heightMap, mutationRateGenerator.applyAsDouble(i), isMutationRateMultiplicative);
        }
        return heightMap;
    }

    public static double[] parseStringToDoubleArray(String input) {
        input = input.replaceAll("[\\s\\[\\]{}]", "");
        String[] stringElementsArray = input.split(",");
        double[] doubleElementsArray = new double[stringElementsArray.length];

        for (int i = 0; i < stringElementsArray.length; i++) {
            doubleElementsArray[i] = Double.parseDouble(stringElementsArray[i]);
        }

        return doubleElementsArray;
    }

    public static void main(String... args) {
        JFrame mainFrame = new JFrame("Midpoint Displayment Demo");

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        JPanel controlPanel = new JPanel();

        {
            controlPanel.setBorder(new LineBorder(Color.RED));
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

            controlPanel.add(new JLabel("Mutation Rates: "));

            // mutationRatesTextArea is a string representing an array of arrays of doubles
            // to add a new double array, add a new line (\n) and to add a new double, just
            // add a new comma
            // each double array will be a new array representing the mutation rates for a
            // new terrain
            JTextArea mutationRatesTextArea = new JTextArea(
                    "{ 2, 2, 1.5, 1 }\n{ 1, 0.5, 0.25, 0.125, 0.0625, 0.03125 }\n2$8$0.9");
            controlPanel.add(mutationRatesTextArea);

            JCheckBox isMultiplicativeCheckBox = new JCheckBox("Is Multiplicative", false);
            controlPanel.add(isMultiplicativeCheckBox);

            JButton regenerateButton = new JButton("Generate");
            regenerateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Generating new terainset. Is Multiplicative is set to "
                            + isMultiplicativeCheckBox.isSelected() + ".");

                    canvas.clear();
                    for (String mutationRatesString : mutationRatesTextArea.getText().split("\n")) {
                        double[] mutationRatesArray;
                        if (mutationRatesString.contains("$")) {
                            // initial rate $ num rates $ roughness
                            double initialRate = Double.parseDouble(mutationRatesString.split("\\$")[0]);
                            int numRates = Integer.parseInt(mutationRatesString.split("\\$")[1]);
                            double roughness = Double.parseDouble(mutationRatesString.split("\\$")[2]);
                            mutationRatesArray = new double[numRates];
                            mutationRatesArray[0] = initialRate;
                            for (int i = 1; i < numRates; i++) {
                                mutationRatesArray[i] = mutationRatesArray[i - 1] * Math.pow(2, -roughness);
                            }
                        } else {
                            mutationRatesArray = parseStringToDoubleArray(mutationRatesString);
                        }
                        double[] heightMap = defaultGenerateHeightmap(mutationRatesArray.length,
                                (i) -> mutationRatesArray[i], isMultiplicativeCheckBox.isSelected());
                        System.out.println("Mutation rates of: " + mutationRatesString + ".");
                        System.out.println("Led to height map: " + Arrays.toString(heightMap) + ".");

                        Polygon terrain = new Polygon(heightMap, DEFAULT_WIDTH, DEFAULT_HEIGHT, -1, 2);

                        terrain.setColor(new Color(
                                Color.HSBtoRGB((float) Math.random(), (float) 1, (float) 1) + (128 << 24), true));
                        // adding 128<<24 is for the opacity value

                        canvas.addShape(terrain);
                    }

                    System.out.println();
                }
            });
            controlPanel.add(regenerateButton);
        }

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.add(controlPanel, BorderLayout.EAST);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }
}
