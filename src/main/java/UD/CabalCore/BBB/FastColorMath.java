package UD.CabalCore.BBB;

public final class FastColorMath {
    private FastColorMath() {
    }

    public static int toWorking(int srgb) {
        return srgb & 255;
    }

    public static int fromAverage(int sum, int count) {
        return clamp255(sum / count);
    }

    public static int clamp255(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    public static int rgba(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}