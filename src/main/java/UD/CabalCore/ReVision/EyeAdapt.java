package UD.CabalCore.ReVision;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public final class EyeAdapt {
    private EyeAdapt() {
    }

    private static float currentExposure = 1.0F;
    private static float currentSceneLuminance = 0.0F;

    private static final float MAX_EXPOSURE_AIR = 1.50F;
    private static final float MAX_EXPOSURE_WATER = 1.03F;

    private static final float DARK_ADAPT_STEP = 0.020F;
    private static final float BRIGHT_ADAPT_STEP = 0.060F;

    private static final float LUMINANCE_RISE_STEP = 0.060F;
    private static final float LUMINANCE_FALL_STEP = 0.010F;

    private static final float ABSOLUTE_BLACK_THRESHOLD = 0.015F;
    private static final float DIM_RANGE = 0.35F;

    // temporary probe
    private static int probeCooldown;
    private static long probeUpdates;
    private static long probeAbsoluteBlackClamps;
    private static long probeMoonBoostApplies;

    private static float probeLastRawSceneLuminance;
    private static float probeLastSceneLuminance;
    private static float probeLastTargetExposure;
    private static float probeLastCurrentExposure;
    private static float probeLastSample0;
    private static float probeLastSample1;
    private static float probeLastSample2;
    private static float probeLastSample3;
    private static float probeLastSample4;
    private static float probeLastSample5;
    private static float probeLastSample6;
    private static boolean probeLastOutdoors;
    private static boolean probeLastUnderwater;
    private static int probeLastMoonPhase;

    public static void update(Minecraft minecraft, float partialTick) {
        if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            currentExposure = 1.0F;
            currentSceneLuminance = 0.0F;
            return;
        }

        ClientLevel level = minecraft.level;
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 eye = camera.getPosition();

        com.mojang.math.Vector3f look = camera.getLookVector();
        Vec3 forward = new Vec3(look.x(), look.y(), look.z()).normalize();

        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        Vec3 sample0Pos = eye;
        Vec3 sample1Pos = eye.add(forward.scale(1.25D));
        Vec3 sample2Pos = eye.add(forward.scale(2.50D));
        Vec3 sample3Pos = eye.add(forward.scale(1.75D)).add(right.scale(0.60D));
        Vec3 sample4Pos = eye.add(forward.scale(1.75D)).subtract(right.scale(0.60D));
        Vec3 sample5Pos = eye.add(right.scale(1.00D));
        Vec3 sample6Pos = eye.subtract(right.scale(1.00D));

        float sample0 = sampleLuminance(level, eye, false);
        float sample1 = sampleLuminance(level, sample1Pos, true);
        float sample2 = sampleLuminance(level, sample2Pos, true);
        float sample3 = sampleLuminance(level, sample3Pos, true);
        float sample4 = sampleLuminance(level, sample4Pos, true);
        float sample5 = sampleLuminance(level, sample5Pos, true);
        float sample6 = sampleLuminance(level, sample6Pos, true);

        float rawSceneLuminance =
                sample0 * 0.20F +
                        sample1 * 0.20F +
                        sample2 * 0.20F +
                        sample3 * 0.10F +
                        sample4 * 0.10F +
                        sample5 * 0.10F +
                        sample6 * 0.10F;

        BlockPos eyePos = new BlockPos(
                Mth.floor(eye.x),
                Mth.floor(eye.y),
                Mth.floor(eye.z)
        );

        boolean outdoors = level.canSeeSky(eyePos.above());
        boolean underwater = minecraft.player.isUnderWater();

        int moonPhase = level.getMoonPhase();
        boolean moonBoosted = false;

        if (outdoors && rawSceneLuminance > 0.0F && rawSceneLuminance < 0.30F) {
            float moonFactor = moonPhaseFactor(moonPhase);
            if (moonFactor > 1.0F) {
                probeMoonBoostApplies++;
                moonBoosted = true;
            }
            rawSceneLuminance *= moonFactor;
        }

        if (rawSceneLuminance > currentSceneLuminance) {
            currentSceneLuminance = Math.min(rawSceneLuminance, currentSceneLuminance + LUMINANCE_RISE_STEP);
        } else {
            currentSceneLuminance = Math.max(rawSceneLuminance, currentSceneLuminance - LUMINANCE_FALL_STEP);
        }

        float maxExposure = underwater ? MAX_EXPOSURE_WATER : MAX_EXPOSURE_AIR;
        float targetExposure;

        if (currentSceneLuminance <= ABSOLUTE_BLACK_THRESHOLD) {
            targetExposure = 1.0F;
            probeAbsoluteBlackClamps++;
        } else {
            float t = 1.0F - Mth.clamp(currentSceneLuminance / DIM_RANGE, 0.0F, 1.0F);
            targetExposure = Mth.lerp(t, 1.0F, maxExposure);
        }

        if (targetExposure > currentExposure) {
            currentExposure = Math.min(targetExposure, currentExposure + DARK_ADAPT_STEP);
        } else {
            currentExposure = Math.max(targetExposure, currentExposure - BRIGHT_ADAPT_STEP);
        }

        probeUpdates++;
        probeLastRawSceneLuminance = rawSceneLuminance;
        probeLastSceneLuminance = currentSceneLuminance;
        probeLastTargetExposure = targetExposure;
        probeLastCurrentExposure = currentExposure;
        probeLastSample0 = sample0;
        probeLastSample1 = sample1;
        probeLastSample2 = sample2;
        probeLastSample3 = sample3;
        probeLastSample4 = sample4;
        probeLastSample5 = sample5;
        probeLastSample6 = sample6;
        probeLastOutdoors = outdoors;
        probeLastUnderwater = underwater;
        probeLastMoonPhase = moonPhase;

        probeCooldown++;
        if (probeCooldown >= 100) {
            probeCooldown = 0;
            System.out.println(
                    "[EyeAdaptation] updates=" + probeUpdates +
                            " rawSceneLum=" + probeLastRawSceneLuminance +
                            " sceneLum=" + probeLastSceneLuminance +
                            " targetExp=" + probeLastTargetExposure +
                            " currentExp=" + probeLastCurrentExposure +
                            " s0=" + probeLastSample0 +
                            " s1=" + probeLastSample1 +
                            " s2=" + probeLastSample2 +
                            " s3=" + probeLastSample3 +
                            " s4=" + probeLastSample4 +
                            " s5=" + probeLastSample5 +
                            " s6=" + probeLastSample6 +
                            " outdoors=" + probeLastOutdoors +
                            " underwater=" + probeLastUnderwater +
                            " moonPhase=" + probeLastMoonPhase +
                            " absBlackClamps=" + probeAbsoluteBlackClamps +
                            " moonBoostApplies=" + probeMoonBoostApplies +
                            " moonBoostedNow=" + moonBoosted
            );
        }
    }

    private static float sampleLuminance(ClientLevel level, Vec3 pos, boolean reduceSkyIndoors) {
        BlockPos blockPos = new BlockPos(
                Mth.floor(pos.x),
                Mth.floor(pos.y),
                Mth.floor(pos.z)
        );

        int blockLight = level.getBrightness(LightLayer.BLOCK, blockPos);
        int skyLight = level.getBrightness(LightLayer.SKY, blockPos);

        boolean outdoors = level.canSeeSky(blockPos.above());

        float skyDarken = level.getSkyDarken(1.0F);
        float effectiveSky = (skyLight / 15.0F) * (skyDarken * 0.95F + 0.05F);
        float effectiveBlock = blockLight / 15.0F;

        if (reduceSkyIndoors && !outdoors) {
            effectiveSky *= 0.10F;
        }

        return Math.max(effectiveBlock, effectiveSky);
    }

    public static int cacheKey() {
        return Math.round(currentExposure * 4096.0F) ^ (Math.round(currentSceneLuminance * 4096.0F) << 1);
    }

    private static float moonPhaseFactor(int moonPhase) {
        return switch (moonPhase) {
            case 0 -> 1.08F;
            case 1, 7 -> 1.02F;
            case 2, 6 -> 1.01F;
            default -> 1.00F;
        };
    }

    public static int applyToPackedColor(int rgba) {
        int a = (rgba >>> 24) & 255;
        int r = rgba & 255;
        int g = (rgba >>> 8) & 255;
        int b = (rgba >>> 16) & 255;

        float rf = r / 255.0F;
        float gf = g / 255.0F;
        float bf = b / 255.0F;

        float luminance = rf * 0.2126F + gf * 0.7152F + bf * 0.0722F;

        if (luminance <= ABSOLUTE_BLACK_THRESHOLD) {
            return rgba;
        }

        float dimWeight = Mth.clamp((DIM_RANGE - luminance) / DIM_RANGE, 0.0F, 1.0F);
        float gain = 1.0F + (currentExposure - 1.0F) * dimWeight * 4.0F;

        rf = Mth.clamp(rf * gain, 0.0F, 1.0F);
        gf = Mth.clamp(gf * gain, 0.0F, 1.0F);
        bf = Mth.clamp(bf * gain, 0.0F, 1.0F);

        int nr = (int)(rf * 255.0F);
        int ng = (int)(gf * 255.0F);
        int nb = (int)(bf * 255.0F);

        return (a << 24) | (nb << 16) | (ng << 8) | nr;
    }
}