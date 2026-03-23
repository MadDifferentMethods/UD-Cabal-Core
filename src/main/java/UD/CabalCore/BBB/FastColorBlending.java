package UD.CabalCore.BBB;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class FastColorBlending {
    public static final int SAMPLE_DIM = 11;
    public static final int OUTPUT_DIM = 5;
    public static final int BOX_DIM = 7;
    public static final int BOX_COUNT = BOX_DIM * BOX_DIM * BOX_DIM;

    private static final ThreadLocal<BlendScratch> SCRATCH =
            ThreadLocal.withInitial(BlendScratch::new);

    private static final int[] OFFSETS_X = {2, 3, 4, 5};
    private static final int[] OFFSETS_Y = {2, 4, 3, 5};
    private static final int[] OFFSETS_Z = {2, 5, 3, 4};

    private FastColorBlending() {
    }

    public static FastBlendChunk generate(Level world, ColorResolver resolver, int chunkX, int chunkY, int chunkZ) {
        BlendScratch scratch = SCRATCH.get();

        int[] lr = scratch.lr;
        int[] lg = scratch.lg;
        int[] lb = scratch.lb;
        int[] workR = scratch.workR;
        int[] workG = scratch.workG;
        int[] workB = scratch.workB;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int sectionBaseX = 4 * (chunkX - 1);
        int sectionBaseY = 4 * (chunkY - 1);
        int sectionBaseZ = 4 * (chunkZ - 1);

        for (int y = 0; y < SAMPLE_DIM; y++) {
            for (int z = 0; z < SAMPLE_DIM; z++) {
                for (int x = 0; x < SAMPLE_DIM; x++) {
                    int sectionX = sectionBaseX + x;
                    int sectionY = sectionBaseY + y;
                    int sectionZ = sectionBaseZ + z;

                    int sampleX = samplePosX(sectionX);
                    int sampleY = samplePosY(sectionY);
                    int sampleZ = samplePosZ(sectionZ);

                    pos.set(sampleX, sampleY, sampleZ);

                    Biome biome = getBiomeOrDefault(world, pos);
                    int color = resolver.getColor(biome, sampleX, sampleZ);

                    int idx = index11(x, y, z);
                    lr[idx] = FastColorMath.toWorking((color >> 16) & 255);
                    lg[idx] = FastColorMath.toWorking((color >> 8) & 255);
                    lb[idx] = FastColorMath.toWorking(color & 255);
                }
            }
        }

        boxBlurX(lr, workR);
        boxBlurX(lg, workG);
        boxBlurX(lb, workB);

        boxBlurZ(workR, lr);
        boxBlurZ(workG, lg);
        boxBlurZ(workB, lb);

        boxBlurY(lr, workR);
        boxBlurY(lg, workG);
        boxBlurY(lb, workB);

        FastBlendChunk out = new FastBlendChunk();

        for (int y = 0; y < OUTPUT_DIM; y++) {
            for (int z = 0; z < OUTPUT_DIM; z++) {
                for (int x = 0; x < OUTPUT_DIM; x++) {
                    int idx11 = index11(x, y, z);

                    int r = FastColorMath.fromAverage(workR[idx11], BOX_COUNT);
                    int g = FastColorMath.fromAverage(workG[idx11], BOX_COUNT);
                    int b = FastColorMath.fromAverage(workB[idx11], BOX_COUNT);

                    out.colors[FastBlendChunk.index(x, y, z)] = FastColorMath.rgba(r, g, b);
                }
            }
        }

        return out;
    }

    private static void boxBlurX(int[] src, int[] dst) {
        for (int y = 0; y < SAMPLE_DIM; y++) {
            for (int z = 0; z < SAMPLE_DIM; z++) {
                int acc = 0;
                for (int x = 0; x < BOX_DIM; x++) {
                    acc += src[index11(x, y, z)];
                }

                for (int x = 0; x < OUTPUT_DIM; x++) {
                    int idx = index11(x, y, z);
                    dst[idx] = acc;

                    if (x < OUTPUT_DIM - 1) {
                        acc -= src[index11(x, y, z)];
                        acc += src[index11(x + BOX_DIM, y, z)];
                    }
                }
            }
        }
    }

    private static void boxBlurZ(int[] src, int[] dst) {
        for (int y = 0; y < SAMPLE_DIM; y++) {
            for (int x = 0; x < OUTPUT_DIM; x++) {
                int acc = 0;
                for (int z = 0; z < BOX_DIM; z++) {
                    acc += src[index11(x, y, z)];
                }

                for (int z = 0; z < OUTPUT_DIM; z++) {
                    int idx = index11(x, y, z);
                    dst[idx] = acc;

                    if (z < OUTPUT_DIM - 1) {
                        acc -= src[index11(x, y, z)];
                        acc += src[index11(x, y, z + BOX_DIM)];
                    }
                }
            }
        }
    }

    private static void boxBlurY(int[] src, int[] dst) {
        for (int z = 0; z < OUTPUT_DIM; z++) {
            for (int x = 0; x < OUTPUT_DIM; x++) {
                int acc = 0;
                for (int y = 0; y < BOX_DIM; y++) {
                    acc += src[index11(x, y, z)];
                }

                for (int y = 0; y < OUTPUT_DIM; y++) {
                    int idx = index11(x, y, z);
                    dst[idx] = acc;

                    if (y < OUTPUT_DIM - 1) {
                        acc -= src[index11(x, y, z)];
                        acc += src[index11(x, y + BOX_DIM, z)];
                    }
                }
            }
        }
    }

    private static int samplePosX(int section) {
        return (section << 2) + OFFSETS_X[(section * 1664525) & 3];
    }

    private static int samplePosY(int section) {
        return (section << 2) + OFFSETS_Y[(section * 214013) & 3];
    }

    private static int samplePosZ(int section) {
        return (section << 2) + OFFSETS_Z[(section * 16807) & 3];
    }

    private static int index11(int x, int y, int z) {
        return x + z * SAMPLE_DIM + y * SAMPLE_DIM * SAMPLE_DIM;
    }

    private static Biome getBiomeOrDefault(Level world, BlockPos pos) {
        Holder<Biome> holder = world.getBiome(pos);
        if (holder.isBound()) {
            return holder.value();
        }

        Holder<Biome> fallback = world.registryAccess()
                .registryOrThrow(Registry.BIOME_REGISTRY)
                .getHolderOrThrow(Biomes.PLAINS);
        return fallback.value();
    }
}