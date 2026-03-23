package UD.CabalCore.BBB;

public final class FastBlendChunk {
    public static final int DIM = 5;
    public static final int COUNT = DIM * DIM * DIM;

    public final int[] colors = new int[COUNT];
    public long key = Long.MIN_VALUE;

    public static int index(int x, int y, int z) {
        return x + z * DIM + y * DIM * DIM;
    }

    public int sampleColor(int blockX, int blockY, int blockZ) {
        int localX = blockX & 15;
        int localY = blockY & 15;
        int localZ = blockZ & 15;

        int sx = localX >> 2;
        int sy = localY >> 2;
        int sz = localZ >> 2;

        int ox = localX & 3;
        int oy = localY & 3;
        int oz = localZ & 3;

        int w0 = (4 - ox) * (4 - oz) * (4 - oy);
        int w1 = ox * (4 - oz) * (4 - oy);
        int w2 = (4 - ox) * oz * (4 - oy);
        int w3 = ox * oz * (4 - oy);
        int w4 = (4 - ox) * (4 - oz) * oy;
        int w5 = ox * (4 - oz) * oy;
        int w6 = (4 - ox) * oz * oy;
        int w7 = ox * oz * oy;

        int c0 = colors[index(sx,     sy,     sz)];
        int c1 = colors[index(sx + 1, sy,     sz)];
        int c2 = colors[index(sx,     sy,     sz + 1)];
        int c3 = colors[index(sx + 1, sy,     sz + 1)];
        int c4 = colors[index(sx,     sy + 1, sz)];
        int c5 = colors[index(sx + 1, sy + 1, sz)];
        int c6 = colors[index(sx,     sy + 1, sz + 1)];
        int c7 = colors[index(sx + 1, sy + 1, sz + 1)];

        int r = (
                ((c0 >> 16) & 255) * w0 +
                        ((c1 >> 16) & 255) * w1 +
                        ((c2 >> 16) & 255) * w2 +
                        ((c3 >> 16) & 255) * w3 +
                        ((c4 >> 16) & 255) * w4 +
                        ((c5 >> 16) & 255) * w5 +
                        ((c6 >> 16) & 255) * w6 +
                        ((c7 >> 16) & 255) * w7
        ) >> 6;

        int g = (
                ((c0 >> 8) & 255) * w0 +
                        ((c1 >> 8) & 255) * w1 +
                        ((c2 >> 8) & 255) * w2 +
                        ((c3 >> 8) & 255) * w3 +
                        ((c4 >> 8) & 255) * w4 +
                        ((c5 >> 8) & 255) * w5 +
                        ((c6 >> 8) & 255) * w6 +
                        ((c7 >> 8) & 255) * w7
        ) >> 6;

        int b = (
                (c0 & 255) * w0 +
                        (c1 & 255) * w1 +
                        (c2 & 255) * w2 +
                        (c3 & 255) * w3 +
                        (c4 & 255) * w4 +
                        (c5 & 255) * w5 +
                        (c6 & 255) * w6 +
                        (c7 & 255) * w7
        ) >> 6;

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}