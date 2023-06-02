package simple;

import java.util.Random;
import java.util.function.DoubleUnaryOperator;

public final class MathUtils {
    private MathUtils() {
        // to make it impossible to have instances of this class
    };

    public static double random(long seed, double origin, double bound) {
        /*
         * Java's default random is kinda bad for this purpose since it
         * 1: requires an object for deterministic results
         * (setting the seed)
         * 2: returns similar outputs for similar inputs
         * 1 is just annoying
         * 2 is fixed by hopefully achieving a chaotic system
         * (idk if that is what chaotic system means)
         * ^ I wonder if I'll ever look back at my comments when I become better at math
         * and what I will think. I hope I find pleasure weird comments like these.
         */
        return new Random(new Random(new Random(seed).nextLong()).nextLong()).nextDouble(origin, bound);
    }

    public static double random(double origin, double bound, long... seeds) {
        // kinda worried that this might have noticable patterns
        // my first idea of using the sum of seeds as a new seed would have obvious
        // patterns so idk

        long trackedSeed = 0;
        for (long seed : seeds) {
            // i sure hope that addition in consequence is enough
            trackedSeed = new Random(new Random(trackedSeed + seed).nextLong()).nextLong();
        }

        return random(trackedSeed, origin, bound);
    }

    public static double sum(double... nums) {
        double sum = 0;
        for (double num : nums) {
            sum += num;
        }
        return sum;
    }

    public static double clamp(double x) {
        return Math.max(0, Math.min(1, x));
    }

    public static double[] apply(double[] arr, DoubleUnaryOperator function) {
        // use stream for elegance?

        double[] applied = new double[arr.length];

        for (int i = 0; i < arr.length; i++) {
            applied[i] = function.applyAsDouble(arr[i]);
        }

        return applied;
    }

    public static double mapToRange(double point, double initialMin, double initialMax, double newMin,
            double newMax) {
        if (newMax == newMin) {
            // logger.warning("New max and min were same.");
            return newMin;
        } else if (Double.isInfinite(initialMin)) {
            // assume this is negative and the only infinite param
            return newMax;
        } else if (Double.isInfinite(initialMax)) {
            // assume this is positive and the only infinite param
            return newMin;
        }

        if (initialMax == initialMin) {
            initialMin = 0;
            initialMax = 1;
            Canvas.logger.warning("Initial max and min were same.");
        }
        return newMin + ((point - initialMin) / (initialMax - initialMin)) * (newMax - newMin);
    }

    public static double max(double... nums) {
        double max = 0;

        for (double num : nums) {
            max = Math.max(num, max);
        }

        return max;
    }
}
