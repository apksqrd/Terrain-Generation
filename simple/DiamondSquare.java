package simple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import simple.Canvas.ColorGradient;
import simple.Canvas.PixelGrid;

public class DiamondSquare {
    public static final int RECOMMENDED_MAX_DEFINITION = 1024;

    public static double[][] generateHeightMap(int sizeFactor, double initialRandomness, double roughnessFactor) {
        double[][] heightMap = new double[(int) Math.pow(2, sizeFactor) + 1][(int) Math.pow(2, sizeFactor) + 1];
        heightMap[0][0] = Math.random();
        heightMap[0][heightMap.length - 1] = Math.random();
        heightMap[heightMap.length - 1][0] = Math.random();
        heightMap[heightMap.length - 1][heightMap.length - 1] = Math.random();

        recurSquareStep(heightMap, heightMap.length - 1, initialRandomness, roughnessFactor);
        return heightMap;
    }

    /**
     * @param heightMap
     * @param rowMin            inclusive
     * @param colMin            inclusive
     * @param rowMax            inclusive
     * @param colMax            inclusive
     * @param currentRandomness
     * @param roughnessFactor
     */
    private static void recurSquareStep(double[][] heightMap, int stepSize, double currentRandomness,
            double roughnessFactor) {
        if (stepSize <= 1) {
            return;
        }

        int halfSize = stepSize / 2;

        for (int row = halfSize; row < heightMap.length; row += stepSize) {
            for (int col = halfSize; col < heightMap[0].length; col += stepSize) {
                double average = (heightMap[row - halfSize][col - halfSize] + heightMap[row - halfSize][col + halfSize]
                        + heightMap[row + halfSize][col - halfSize] + heightMap[row + halfSize][col + halfSize]) / 4;

                heightMap[row][col] = Canvas.mapToRange(
                        Math.random(), 0, 1, average - currentRandomness, average + currentRandomness);
                heightMap[row][col] = average;
            }
        }

        recurDiamondStep(heightMap, stepSize, currentRandomness, roughnessFactor);
    }

    private static void recurDiamondStep(double[][] heightMap, int stepSize, double currentRandomness,
            double roughnessFactor) {
        if (stepSize <= 1) {
            return;
        }

        int halfSize = stepSize / 2;

        for (int row = halfSize; row < heightMap.length; row += stepSize) {
            for (int col = 0; col < heightMap[0].length; col += stepSize) {
                // changing rows are guranteed
                // changing col is not
                int numNear = 2;
                double sum = heightMap[row - halfSize][col] + heightMap[row + halfSize][col];
                if (col - halfSize >= 0) {
                    sum += heightMap[row][col - halfSize];
                    numNear++;
                }
                if (col + halfSize < heightMap.length) {
                    sum += heightMap[row][col + halfSize];
                    numNear++;
                }

                double average = sum / numNear;

                heightMap[row][col] = Canvas.mapToRange(
                        Math.random(), 0, 1, average - currentRandomness, average + currentRandomness);
            }
        }

        for (int row = 0; row < heightMap.length; row += stepSize) {
            for (int col = halfSize; col < heightMap[0].length; col += stepSize) {
                // changing col is guranteed
                // changing row is not
                int numNear = 2;
                double sum = heightMap[row][col - halfSize] + heightMap[row][col + halfSize];
                if (row - halfSize >= 0) {
                    sum += heightMap[row - halfSize][col];
                    numNear++;
                }
                if (row + halfSize < heightMap.length) {
                    sum += heightMap[row + halfSize][col];
                    numNear++;
                }

                double average = sum / numNear;

                heightMap[row][col] = Canvas.mapToRange(
                        Math.random(), 0, 1, average - currentRandomness, average + currentRandomness);
            }
        }

        recurSquareStep(heightMap, halfSize, currentRandomness * roughnessFactor, roughnessFactor);
    }

    public static double[][] increaseDefinition(double[][] heightMap, double currentRandomness) {
        // TODO: check non-square and even length works

        double[][] definedHeightMap = new double[2 * heightMap.length - 1][2 * heightMap[0].length - 1];

        for (int row = 0; row < heightMap.length; row++) {
            for (int col = 0; col < heightMap.length; col++) {
                definedHeightMap[2 * row][2 * col] = heightMap[row][col];
            }
        }

        squareStep: for (int row = 1; row < definedHeightMap.length; row += 2) {
            for (int col = 1; col < definedHeightMap[0].length; col += 2) {
                double average = (definedHeightMap[row - 1][col - 1] + definedHeightMap[row - 1][col + 1]
                        + definedHeightMap[row + 1][col - 1] + definedHeightMap[row + 1][col + 1]) / 4;

                definedHeightMap[row][col] = Canvas.mapToRange(
                        Math.random(), 0, 1, average - currentRandomness, average + currentRandomness);
                definedHeightMap[row][col] = average;
            }
        }

        diamondStep: {
            for (int row = 1; row < definedHeightMap.length; row += 2) {
                for (int col = 0; col < definedHeightMap[0].length; col += 2) {
                    // changing rows are guranteed
                    // changing col is not
                    int numNear = 2;
                    double sum = definedHeightMap[row - 1][col] + definedHeightMap[row + 1][col];
                    if (col - 1 >= 0) {
                        sum += definedHeightMap[row][col - 1];
                        numNear++;
                    }
                    if (col + 1 < definedHeightMap.length) {
                        sum += definedHeightMap[row][col + 1];
                        numNear++;
                    }

                    double average = sum / numNear;

                    definedHeightMap[row][col] = Canvas.mapToRange(
                            Math.random(), 0, 1, average - currentRandomness, average + currentRandomness);
                }
            }

            for (int row = 0; row < definedHeightMap.length; row += 2) {
                for (int col = 1; col < definedHeightMap[0].length; col += 2) {
                    // changing col is guranteed
                    // changing row is not
                    int numNear = 2;
                    double sum = definedHeightMap[row][col - 1] + definedHeightMap[row][col + 1];
                    if (row - 1 >= 0) {
                        sum += definedHeightMap[row - 1][col];
                        numNear++;
                    }
                    if (row + 1 < definedHeightMap.length) {
                        sum += definedHeightMap[row + 1][col];
                        numNear++;
                    }

                    double average = sum / numNear;

                    definedHeightMap[row][col] = Canvas.mapToRange(
                            Math.random(), 0, 1, average - currentRandomness, average + currentRandomness);
                }
            }
        }

        return definedHeightMap;
    }

    public static void singleDemo() {
        System.out.println(Arrays.deepToString(generateHeightMap(1, 0, 1)));

        JFrame mainFrame = new JFrame("Diamond Square Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(640, 640));

        JPanel controlPanel = new JPanel();

        {
            controlPanel.setBorder(new LineBorder(Color.RED));
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

            controlPanel.add(new JLabel("Parameters (sizeFactor$initialRandomness$roughnessFactor):"));

            JTextArea parametersTextArea = new JTextArea(
                    "7$1.0$0.5");
            controlPanel.add(parametersTextArea);

            JButton regenerateButton = new JButton("Generate");
            regenerateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    canvas.clear();

                    String parameterString = parametersTextArea.getText();
                    if (parameterString.contains("$")) {
                        int sizeFactor = Integer.parseInt(parameterString.split("\\$")[0]);
                        double initialRandomness = Double.parseDouble(parameterString.split("\\$")[1]);
                        double roughnessFactor = Double.parseDouble(parameterString.split("\\$")[2]);
                        canvas.addShape(new PixelGrid(generateHeightMap(sizeFactor, initialRandomness, roughnessFactor),
                                0, 0, 640,
                                640, PixelGrid::coloredRGBGenerator));
                    }
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

    public static void createTerrainEditor(double[][] heightMap, int displayWidth, int displayHeight) {
        JFrame terrain = new JFrame("Diamond Square Terrain Editor");

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(640, 640));

        // i don't know what the atomic part of this does, but i am using this because
        // anything i reference in an enclosing scope must be final (idk either)
        AtomicReference<double[][]> heightMapReference = new AtomicReference<double[][]>(heightMap);

        canvas.addShape(new PixelGrid(heightMap, 0, 0, displayWidth, displayHeight,
                ColorGradient.createTerrainStyleGradient()));

        JPanel controlPanel = new JPanel();

        {
            controlPanel.setBorder(new LineBorder(Color.RED));
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

            controlPanel.add(new JLabel("Randomness: "));

            JTextArea randomnessTextArea = new JTextArea(
                    "0.5");
            controlPanel.add(randomnessTextArea);

            controlPanel.add(new JLabel("Auto Roughness: "));

            JTextArea autoRoughnessTextArea = new JTextArea(
                    "0.5");
            controlPanel.add(autoRoughnessTextArea);

            JButton regenerateButton = new JButton("Increase Definition");
            regenerateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (2 * heightMapReference.get().length - 1 > RECOMMENDED_MAX_DEFINITION
                            || 2 * heightMapReference.get()[0].length - 1 > RECOMMENDED_MAX_DEFINITION) {
                        int selectedOption = JOptionPane.showConfirmDialog(null,
                                "Next iteration will lead to definition higher than the recommended: "
                                        + RECOMMENDED_MAX_DEFINITION + ". Currently :"
                                        + Math.max(heightMapReference.get().length, heightMapReference.get()[0].length)
                                        + ".",
                                "Select an Option", JOptionPane.OK_CANCEL_OPTION);

                        if (selectedOption != JOptionPane.OK_OPTION) {
                            return;
                        }
                    }

                    canvas.clear();

                    heightMapReference.set(increaseDefinition(heightMapReference.get(),
                            Double.parseDouble(randomnessTextArea.getText())));

                    randomnessTextArea
                            .setText("" +
                                    Double.parseDouble(randomnessTextArea.getText())
                                            * Double.parseDouble(autoRoughnessTextArea.getText()));

                    canvas.addShape(
                            new PixelGrid(heightMapReference.get(), 0, 0, 640, 640,
                                    ColorGradient.createTerrainStyleGradient()));
                }
            });
            controlPanel.add(regenerateButton);
        }

        terrain.setLayout(new BorderLayout());
        terrain.add(canvas, BorderLayout.CENTER);
        terrain.add(controlPanel, BorderLayout.EAST);
        terrain.pack();

        terrain.setVisible(true);

        // TODO: save, undo, allow to change initial array, click to manually change
        // values
    }

    private static double[][] parseDoubleMatrix(String input) {
        input = input.replaceAll("[\\s\\[\\]{}]", "");

        String[] rowsStringArray = input.split(";");
        double[][] output = new double[rowsStringArray.length][];

        for (int i = 0; i < rowsStringArray.length; i++) {
            String[] rowString = rowsStringArray[i].split(",");
            double[] row = new double[rowString.length];

            for (int j = 0; j < rowString.length; j++) {
                row[j] = Double.parseDouble(rowString[j]);
            }

            output[i] = row;
        }

        return output;
    }

    public static void createTerrainEditorGenerator() {
        JFrame mainFrame = new JFrame("Diamond Square Terrain Editor Generator");
        JPanel controlPanel = new JPanel(); // boxlayout doesn't worwk with jframe
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        // trust users submit perfect rectangular matrices
        JTextArea initialHeightMapTextArea = new JTextArea("0,1;\n1,0");

        JButton generateButton = new JButton("New Terrain Editor");
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createTerrainEditor(parseDoubleMatrix(initialHeightMapTextArea.getText()), 640, 640);
            }
        });

        controlPanel.add(initialHeightMapTextArea);
        controlPanel.add(generateButton);

        mainFrame.add(controlPanel);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static void main(String... args) {
        createTerrainEditorGenerator();
    }
}
