package UD.CabalCore.BBB;

public final class BlendScratch {
    public static final int SAMPLE_DIM = 11;
    public static final int SAMPLE_COUNT = SAMPLE_DIM * SAMPLE_DIM * SAMPLE_DIM;

    public final int[] lr = new int[SAMPLE_COUNT];
    public final int[] lg = new int[SAMPLE_COUNT];
    public final int[] lb = new int[SAMPLE_COUNT];

    public final int[] workR = new int[SAMPLE_COUNT];
    public final int[] workG = new int[SAMPLE_COUNT];
    public final int[] workB = new int[SAMPLE_COUNT];
}