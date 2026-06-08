package noppes.npcs.shared.common.util;

/**
 * https://github.com/ai/easings.net
 * https://easings.net/
 */

public class EasingFunctions {

    public static float easeInCubic(float x) {
        return x * x * x;
    }

    public static float easeOutCubic(float x) {
        return 1 - (float)Math.pow(1 - x, 3);
    }

    public static float easeInOutCubic(float x) {
        return x < 0.5 ? 4 * x * x * x : 1 - (float)Math.pow(-2 * x + 2, 3) / 2;
    }

    public static float easeInOutQuad(float x) {
        return x < 0.5 ? 2 * x * x : 1 - (float)Math.pow(-2 * x + 2, 2) / 2;
    }

    public static float easeInOutQuart(float x) {
        return x < 0.5 ? 8 * x * x * x * x : 1 - (float)Math.pow(-2 * x + 2, 4) / 2;
    }
}
