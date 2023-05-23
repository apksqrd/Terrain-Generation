package simple;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.JFrame;

import simple.Canvas.PixelGrid;

public class DiamondSquare {
    public static double[][] generateHeightMap(int sizeFactor, double initialRandomness, double roughnessFactor) {
        double[][] heightMap = new double[(int) Math.pow(2, sizeFactor) + 1][(int) Math.pow(2, sizeFactor) + 1];
        heightMap[0][0] = Math.random();
        heightMap[0][heightMap.length - 1] = Math.random();
        heightMap[heightMap.length - 1][0] = Math.random();
        heightMap[heightMap.length - 1][heightMap.length - 1] = Math.random();

        diamondStep(heightMap, heightMap.length - 1, initialRandomness, roughnessFactor);
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
    private static void diamondStep(double[][] heightMap, int stepSize, double currentRandomness,
            double roughnessFactor) {
        if (stepSize <= 1) {
            return;
        }

        int halfSize = stepSize / 2;

        for (int row = halfSize; row < heightMap.length; row += stepSize) {
            for (int col = halfSize; col < heightMap.length; col += stepSize) {
                double average = (heightMap[row - halfSize][col - halfSize] + heightMap[row - halfSize][col + halfSize]
                        + heightMap[row + halfSize][col - halfSize] + heightMap[row + halfSize][col + halfSize]) / 4;

                heightMap[row][col] = Canvas.mapToRange(
                        Math.random(), 0, 1, average - currentRandomness, average + currentRandomness);
                heightMap[row][col] = average;
            }
        }

        squareStep(heightMap, stepSize, currentRandomness, roughnessFactor);
    }

    private static void squareStep(double[][] heightMap, int stepSize, double currentRandomness,
            double roughnessFactor) {
        if (stepSize <= 1) {
            return;
        }

        int halfSize = stepSize / 2;

        for (int row = halfSize; row < heightMap.length; row += stepSize) {
            for (int col = 0; col < heightMap.length; col += stepSize) {
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
            for (int col = halfSize; col < heightMap.length; col += stepSize) {
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

        diamondStep(heightMap, halfSize, currentRandomness * roughnessFactor, roughnessFactor);
    }

    public double[][] increaseDefinition(double[][] heightMap, double currentRandomness) {
        return null;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(generateHeightMap(1, 0, 1)));

        JFrame mainFrame = new JFrame("Diamond Square Demo");

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(640, 640));

        canvas.addShape(new PixelGrid(generateHeightMap(10, 0.5, 0.99), 0, 0, 640,
                640));

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }
}
