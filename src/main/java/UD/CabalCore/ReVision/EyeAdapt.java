package UD.CabalCore.ReVision;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class EyeAdapt {
    private EyeAdapt() {
    }

    private static float currentExposure = 1.0F;
    private static float currentDarkForestFactor = 0.0F;

    private static final float UNDERGROUND_MAX_EXPOSURE = 1.22F;
    private static final float OVERWORLD_NIGHT_MAX_BASE = 1.08F;
    private static final float UNDERWATER_MAX_EXPOSURE = 1.00F;

    private static final float DARK_FOREST_BASELINE = 0.85F;

    private static final float DARK_ADAPT_STEP = 0.00010F;
    private static final float BRIGHT_ADAPT_STEP = 0.015F;

    private static final float DARK_FOREST_RISE_STEP = 0.003F;
    private static final float DARK_FOREST_FALL_STEP = 0.002F;

    private static final float ABSOLUTE_BLACK_THRESHOLD = 0.015F;

    // temporary probe
    private static int probeCooldown;
    private static long probeUpdates;
    private static long probeAbsoluteBlackPixels;

    private static float probeLastSceneLuminance;
    private static float probeLastTargetExposure;
    private static float probeLastCurrentExposure;
    private static float probeLastSkyDarken;
    private static float probeLastMoonFactor;
    private static float probeLastDarkForestFactor;
    private static float probeLastDarkForestTargetFactor;
    private static boolean probeLastOutdoors;
    private static boolean probeLastUnderwater;
    private static boolean probeLastOverworld;
    private static boolean probeLastDarkForest;
    private static int probeLastMoonPhase;
    private static int probeLastBlockLight;
    private static int probeLastSkyLight;

    public static void update(Minecraft minecraft, float partialTick) {
        if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            currentExposure = 1.0F;
            currentDarkForestFactor = 0.0F;
            return;
        }

        ClientLevel level = minecraft.level;

        BlockPos eyePos = minecraft.player.blockPosition().above();
        int blockLight = level.getBrightness(LightLayer.BLOCK, eyePos);
        int skyLight = level.getBrightness(LightLayer.SKY, eyePos);

        boolean outdoors = level.canSeeSky(eyePos);
        boolean underwater = minecraft.player.isUnderWater();
        boolean overworld = level.dimension() == Level.OVERWORLD;

        Holder<Biome> biome = level.getBiome(eyePos);
        boolean darkForest = biome.is(Biomes.DARK_FOREST);

        float skyDarken = level.getSkyDarken(1.0F);
        int moonPhase = level.getMoonPhase();
        float moonFactor = moonPhaseFactor(moonPhase);

        float sceneLuminance = localSceneLuminance(blockLight, skyLight, outdoors, skyDarken);

        // Continuous canopy factor instead of hard toggle.
        // 0 skylight => 0, 15 skylight => 1, smooth in-between.
        float darkForestTargetFactor = 0.0F;
        if (overworld && darkForest) {
            darkForestTargetFactor = Mth.clamp((skyLight - 2.0F) / 10.0F, 0.0F, 1.0F);
        }

        if (darkForestTargetFactor > currentDarkForestFactor) {
            currentDarkForestFactor = Math.min(darkForestTargetFactor, currentDarkForestFactor + DARK_FOREST_RISE_STEP);
        } else {
            currentDarkForestFactor = Math.max(darkForestTargetFactor, currentDarkForestFactor - DARK_FOREST_FALL_STEP);
        }

        float targetExposure;

        if (underwater) {
            targetExposure = UNDERWATER_MAX_EXPOSURE;
        } else if (!outdoors) {
            // Underground / indoors baseline.
            targetExposure = UNDERGROUND_MAX_EXPOSURE;

            // But if this is still Dark Forest canopy, blend back toward the forest regime.
            if (currentDarkForestFactor > 0.0F) {
                targetExposure = Mth.lerp(currentDarkForestFactor, UNDERGROUND_MAX_EXPOSURE, DARK_FOREST_BASELINE);
            }
        } else if (outdoors && overworld) {
            float nightFactor = 1.0F - Mth.clamp(skyLight / 15.0F, 0.0F, 1.0F);
            float outdoorNightMax = OVERWORLD_NIGHT_MAX_BASE * moonFactor;
            float outdoorTarget = Mth.lerp(nightFactor, 1.0F, outdoorNightMax);

            if (currentDarkForestFactor > 0.0F) {
                targetExposure = Mth.lerp(currentDarkForestFactor, outdoorTarget, DARK_FOREST_BASELINE);
            } else {
                targetExposure = outdoorTarget;
            }
        } else {
            targetExposure = 1.0F;
        }

        if (targetExposure > currentExposure) {
            currentExposure = Math.min(targetExposure, currentExposure + DARK_ADAPT_STEP);
        } else {
            currentExposure = Math.max(targetExposure, currentExposure - BRIGHT_ADAPT_STEP);
        }

        probeUpdates++;
        probeLastSceneLuminance = sceneLuminance;
        probeLastTargetExposure = targetExposure;
        probeLastCurrentExposure = currentExposure;
        probeLastSkyDarken = skyDarken;
        probeLastMoonFactor = moonFactor;
        probeLastDarkForestFactor = currentDarkForestFactor;
        probeLastDarkForestTargetFactor = darkForestTargetFactor;
        probeLastOutdoors = outdoors;
        probeLastUnderwater = underwater;
        probeLastOverworld = overworld;
        probeLastDarkForest = darkForest;
        probeLastMoonPhase = moonPhase;
        probeLastBlockLight = blockLight;
        probeLastSkyLight = skyLight;

        probeCooldown++;
        if (probeCooldown >= 100) {
            probeCooldown = 0;
            System.out.println(
                    "[EyeAdaptation] updates=" + probeUpdates +
                            " sceneLum=" + probeLastSceneLuminance +
                            " targetExp=" + probeLastTargetExposure +
                            " currentExp=" + probeLastCurrentExposure +
                            " skyDarken=" + probeLastSkyDarken +
                            " moonFactor=" + probeLastMoonFactor +
                            " darkForestFactor=" + probeLastDarkForestFactor +
                            " darkForestTargetFactor=" + probeLastDarkForestTargetFactor +
                            " outdoors=" + probeLastOutdoors +
                            " underwater=" + probeLastUnderwater +
                            " overworld=" + probeLastOverworld +
                            " darkForest=" + probeLastDarkForest +
                            " moonPhase=" + probeLastMoonPhase +
                            " blockLight=" + probeLastBlockLight +
                            " skyLight=" + probeLastSkyLight +
                            " absBlackPixels=" + probeAbsoluteBlackPixels
            );
        }
    }

    private static float localSceneLuminance(int blockLight, int skyLight, boolean outdoors, float skyDarken) {
        float effectiveBlock = blockLight / 15.0F;
        float effectiveSky = (skyLight / 15.0F) * (skyDarken * 0.95F + 0.05F);

        if (!outdoors) {
            effectiveSky *= 0.10F;
        }

        return Math.max(effectiveBlock, effectiveSky);
    }

    public static int cacheKey() {
        return Math.round(currentExposure * 4096.0F) ^ (Math.round(currentDarkForestFactor * 4096.0F) << 1);
    }

    private static float moonPhaseFactor(int moonPhase) {
        return switch (moonPhase) {
            case 0 -> 1.08F;
            case 1, 7 -> 1.04F;
            case 2, 6 -> 1.02F;
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
            probeAbsoluteBlackPixels++;
            return rgba;
        }

        float gain;
        if (currentExposure >= 1.0F) {
            float dimWeight = Mth.clamp((0.35F - luminance) / 0.35F, 0.0F, 1.0F);
            gain = 1.0F + (currentExposure - 1.0F) * dimWeight * 3.0F;
        } else {
            float darkWeight = Mth.clamp(luminance / 0.40F, 0.0F, 1.0F);
            gain = 1.0F - (1.0F - currentExposure) * darkWeight;
        }

        rf = Mth.clamp(rf * gain, 0.0F, 1.0F);
        gf = Mth.clamp(gf * gain, 0.0F, 1.0F);
        bf = Mth.clamp(bf * gain, 0.0F, 1.0F);

        int nr = (int)(rf * 255.0F);
        int ng = (int)(gf * 255.0F);
        int nb = (int)(bf * 255.0F);

        return (a << 24) | (nb << 16) | (ng << 8) | nr;
    }
}