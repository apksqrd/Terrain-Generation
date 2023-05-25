package simple;

import java.awt.Color;

import simple.Canvas.ColorGradient.SingularColorGradient;

public class Test {
    public static void main(String[] args) {
        System.out.println(Canvas.mapToRange(0, Double.NEGATIVE_INFINITY, 0, 0, 1));
        SingularColorGradient gradient = new SingularColorGradient(Double.NEGATIVE_INFINITY, 0, Color.BLACK,
                Color.WHITE);
        System.out.println(Integer.toBinaryString(Color.WHITE.getRGB()));
        System.out.println(Integer.toBinaryString(gradient.getColor(-0.3)));
    }
}
