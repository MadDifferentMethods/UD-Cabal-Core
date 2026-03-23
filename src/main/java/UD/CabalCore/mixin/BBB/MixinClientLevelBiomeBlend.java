package UD.CabalCore.mixin.BBB;

import UD.CabalCore.BBB.FastBlendCache;
import UD.CabalCore.BBB.FastBlendChunk;
import UD.CabalCore.BBB.FastColorBlending;
import UD.CabalCore.BBB.FinalLookup;
import UD.CabalCore.BBB.ResolverIDs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevelBiomeBlend extends Level {
    @Shadow @Final
    private Minecraft minecraft;

    @Unique
    private static final int BASE_CACHE_MIN = 512;

    @Unique
    private static final int BASE_CACHE_MAX = 8192;

    @Unique
    private static final int INVALIDATION_RADIUS = 1;

    @Unique
    private static final boolean ENABLE_PREFETCH = true;

    @Unique
    private static final boolean PREFETCH_DIAGONALS = false;

    @Unique
    private final FastBlendCache cc$biomeBlendCache = new FastBlendCache(1024);

    @Unique
    private int cc$lastBiomeBlendCacheEntries = -1;

    @Unique
    private final ThreadLocal<FinalLookup> cc$lastBlendLookup =
            ThreadLocal.withInitial(FinalLookup::new);

    @Unique
    private int cc$lastPrefetchChunkX = Integer.MIN_VALUE;

    @Unique
    private int cc$lastPrefetchChunkY = Integer.MIN_VALUE;

    @Unique
    private int cc$lastPrefetchChunkZ = Integer.MIN_VALUE;

    @Unique
    private int cc$lastPrefetchColorType = Integer.MIN_VALUE;

    protected MixinClientLevelBiomeBlend(
            WritableLevelData levelData,
            ResourceKey<Level> dimension,
            Holder<DimensionType> dimensionTypeRegistration,
            Supplier<ProfilerFiller> profiler,
            boolean isClientSide,
            boolean isDebug,
            long biomeZoomSeed,
            int maxChainedNeighborUpdates
    ) {
        super(levelData, dimension, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Unique
    private static int cc$computeCacheEntries(int renderDistance) {
        int paddedDiameter = (renderDistance * 2 + 1) + 4;
        int visibleXZ = paddedDiameter * paddedDiameter;

        int estimate = visibleXZ * 6;

        if (estimate < BASE_CACHE_MIN) estimate = BASE_CACHE_MIN;
        if (estimate > BASE_CACHE_MAX) estimate = BASE_CACHE_MAX;

        return estimate;
    }

    @Unique
    private void cc$syncBiomeBlendCacheSize() {
        int renderDistance = this.minecraft.options.renderDistance().get();
        int desired = cc$computeCacheEntries(renderDistance);

        if (desired != this.cc$lastBiomeBlendCacheEntries) {
            this.cc$lastBiomeBlendCacheEntries = desired;
            this.cc$biomeBlendCache.resize(desired);
        }
    }

    @Inject(method = "clearTintCaches", at = @At("HEAD"))
    private void cc$clearBiomeBlendCache(CallbackInfo ci) {
        cc$syncBiomeBlendCacheSize();
        cc$biomeBlendCache.clear();
    }

    @Inject(method = "onChunkLoaded", at = @At("HEAD"))
    private void cc$invalidateBiomeBlendNeighborhood(ChunkPos chunkPos, CallbackInfo ci) {
        cc$biomeBlendCache.invalidateChunkNeighborhood(chunkPos.x, chunkPos.z, INVALIDATION_RADIUS);
    }

    @Unique
    private void cc$prefetchNeighbors(ClientLevel self, ColorResolver resolver, int chunkX, int chunkY, int chunkZ, int colorType) {
        if (!ENABLE_PREFETCH) {
            return;
        }

        if (chunkX == cc$lastPrefetchChunkX
                && chunkY == cc$lastPrefetchChunkY
                && chunkZ == cc$lastPrefetchChunkZ
                && colorType == cc$lastPrefetchColorType) {
            return;
        }

        cc$lastPrefetchChunkX = chunkX;
        cc$lastPrefetchChunkY = chunkY;
        cc$lastPrefetchChunkZ = chunkZ;
        cc$lastPrefetchColorType = colorType;

        prefetchOne(self, resolver, chunkX + 1, chunkY, chunkZ, colorType);
        prefetchOne(self, resolver, chunkX - 1, chunkY, chunkZ, colorType);
        prefetchOne(self, resolver, chunkX, chunkY, chunkZ + 1, colorType);
        prefetchOne(self, resolver, chunkX, chunkY, chunkZ - 1, colorType);

        if (PREFETCH_DIAGONALS) {
            prefetchOne(self, resolver, chunkX + 1, chunkY, chunkZ + 1, colorType);
            prefetchOne(self, resolver, chunkX + 1, chunkY, chunkZ - 1, colorType);
            prefetchOne(self, resolver, chunkX - 1, chunkY, chunkZ + 1, colorType);
            prefetchOne(self, resolver, chunkX - 1, chunkY, chunkZ - 1, colorType);
        }
    }

    @Unique
    private void prefetchOne(ClientLevel self, ColorResolver resolver, int chunkX, int chunkY, int chunkZ, int colorType) {
        long key = FastBlendCache.packKey(chunkX, chunkY, chunkZ, colorType);
        cc$biomeBlendCache.prefetch(
                key,
                () -> FastColorBlending.generate(self, resolver, chunkX, chunkY, chunkZ)
        );
    }

    /**
     * @author DECAY
     * @reason Replace vanilla biome color blending with cached chunk-based blending.
     */
    @Overwrite
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        cc$syncBiomeBlendCacheSize();

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        int chunkX = x >> 4;
        int chunkY = y >> 4;
        int chunkZ = z >> 4;

        int colorType = ResolverIDs.getId(resolver);
        long key = FastBlendCache.packKey(chunkX, chunkY, chunkZ, colorType);

        int epoch = cc$biomeBlendCache.getEpoch();
        FinalLookup hot = cc$lastBlendLookup.get();

        FastBlendChunk chunk;
        if (hot.chunk != null && hot.key == key && hot.epoch == epoch) {
            chunk = hot.chunk;
        } else {
            ClientLevel self = (ClientLevel) (Object) this;
            chunk = cc$biomeBlendCache.getOrCreate(
                    key,
                    () -> FastColorBlending.generate(self, resolver, chunkX, chunkY, chunkZ)
            );

            hot.key = key;
            hot.epoch = epoch;
            hot.chunk = chunk;

            cc$prefetchNeighbors(self, resolver, chunkX, chunkY, chunkZ, colorType);
        }

        return chunk.sampleColor(x, y, z);
    }
}