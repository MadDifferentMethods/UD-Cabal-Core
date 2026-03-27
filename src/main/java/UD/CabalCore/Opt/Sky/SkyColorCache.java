package UD.CabalCore.Opt.Sky;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SkyColorCache {
    private SkyColorCache() {
    }

    private static boolean init;

    private static int lastLevelId;
    private static int lastEffectsId;

    private static int lastXBucket;
    private static int lastYBucket;
    private static int lastZBucket;

    private static int lastTimeOfDay;
    private static int lastRainLevel;
    private static int lastThunderLevel;

    private static Vec3 lastSkyColor = Vec3.ZERO;

    private static int q(float value, float scale) {
        return Math.round(value * scale);
    }

    private static int bucket(double value) {
        return Mth.floor(value / 4.0D);
    }

    private static Vec3 getCached(ClientLevel level, Vec3 pos, float partialTick) {
        int levelId = System.identityHashCode(level);
        int effectsId = System.identityHashCode(level.effects());

        int xBucket = bucket(pos.x);
        int yBucket = bucket(pos.y);
        int zBucket = bucket(pos.z);

        int timeOfDay = q(level.getTimeOfDay(partialTick), 2048.0F);
        int rainLevel = q(level.getRainLevel(partialTick), 512.0F);
        int thunderLevel = q(level.getThunderLevel(partialTick), 512.0F);

        if (init
                && lastLevelId == levelId
                && lastEffectsId == effectsId
                && lastXBucket == xBucket
                && lastYBucket == yBucket
                && lastZBucket == zBucket
                && lastTimeOfDay == timeOfDay
                && lastRainLevel == rainLevel
                && lastThunderLevel == thunderLevel) {
            return lastSkyColor;
        }

        Vec3 computed = level.getSkyColor(pos, partialTick);

        init = true;
        lastLevelId = levelId;
        lastEffectsId = effectsId;

        lastXBucket = xBucket;
        lastYBucket = yBucket;
        lastZBucket = zBucket;

        lastTimeOfDay = timeOfDay;
        lastRainLevel = rainLevel;
        lastThunderLevel = thunderLevel;

        lastSkyColor = computed;
        return computed;
    }

    public static Vec3 getForFog(ClientLevel level, Vec3 pos, float partialTick) {
        return getCached(level, pos, partialTick);
    }

    public static Vec3 getForSkyRender(ClientLevel level, Vec3 pos, float partialTick) {
        return getCached(level, pos, partialTick);
    }
}