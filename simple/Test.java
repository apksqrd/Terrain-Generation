package simple;

import java.awt.Color;
import java.util.Random;

import simple.Canvas.ColorGradient.SingularColorGradient;

public class Test {
    public static void main(String[] args) {
        System.out.println(MathUtils.mapToRange(0, Double.NEGATIVE_INFINITY, 0, 0, 1));
        SingularColorGradient gradient = new SingularColorGradient(Double.NEGATIVE_INFINITY, 0, Color.BLACK,
                Color.WHITE);
        System.out.println(Integer.toBinaryString(Color.WHITE.getRGB()));
        System.out.println(Integer.toBinaryString(gradient.getColor(-0.3)));

        Random randomNumberGenerator = new Random(12345L);
        System.out.println(randomNumberGenerator.nextDouble());
        System.out.println(randomNumberGenerator.nextDouble());
        randomNumberGenerator.setSeed(12345L);
        System.out.println(randomNumberGenerator.nextDouble());
        System.out.println(randomNumberGenerator.nextDouble());
        randomNumberGenerator.setSeed(12346L);
        System.out.println(randomNumberGenerator.nextDouble());
        System.out.println(randomNumberGenerator.nextDouble());

        System.out.println(Integer.toBinaryString((3 >> 0) & 1));
        System.out.println(Integer.toBinaryString((3 >> 1) & 1));
        System.out.println(Integer.toBinaryString((3 >> 2) & 1));
    }
}
