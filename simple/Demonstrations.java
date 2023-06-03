package simple;

import java.util.Scanner;

public class Demonstrations {
    /**
     * All of the notable demonstrations, in order of earliest to latest to be
     * implemented and therefore least to most complicated.
     */
    static final Runnable[] demonstrations = {
            // Midpoint Displacement
            MidpointDisplacement::main,

            // Diamond Square
            DiamondSquare::createTerrainEditorGenerator,

            // Perlin 1D
            Perlin1D::cubicSlopeInterpolationDemonstration,
            Perlin1D::cubicPerlinDemo,
            Perlin1D::fadeFunctionInterpolationDemo,
            Perlin1D::fadePerlinDemo,

            // General Perlin
            Perlin::interpolationDemo2D,
            Perlin::slopeInterpolationDemo1D,
            Perlin::perlinNoiseDemo1D,
            Perlin::perlinNoiseDemo2D,
    };

    public static void everythingDemonstration() {
        Scanner scanner = new Scanner(System.in); // so it is one at a time

        for (Runnable demonstration : demonstrations) {
            demonstration.run();

            scanner.nextLine();
        }

        scanner.close();
    }

    public static void main(String[] args) {
        everythingDemonstration();
    }
}
