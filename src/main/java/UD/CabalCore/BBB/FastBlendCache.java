package UD.CabalCore.BBB;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public final class FastBlendCache {
    private final Long2ObjectLinkedOpenHashMap<FastBlendChunk> cache;
    private final Long2ObjectOpenHashMap<LongArrayList> byXZ;
    private final ConcurrentHashMap<Long, CompletableFuture<FastBlendChunk>> inFlight;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile int epoch = 0;
    private int maxEntries;

    public FastBlendCache(int maxEntries) {
        this.maxEntries = maxEntries;
        this.cache = new Long2ObjectLinkedOpenHashMap<>(maxEntries);
        this.byXZ = new Long2ObjectOpenHashMap<>();
        this.inFlight = new ConcurrentHashMap<>();
    }

    public int getEpoch() {
        return epoch;
    }

    public FastBlendChunk get(long key) {
        lock.lock();
        try {
            return cache.getAndMoveToFirst(key);
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(long key) {
        lock.lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    public FastBlendChunk getOrCreate(long key, Supplier<FastBlendChunk> generator) {
        FastBlendChunk cached = get(key);
        if (cached != null) {
            return cached;
        }

        CompletableFuture<FastBlendChunk> mine = new CompletableFuture<>();
        CompletableFuture<FastBlendChunk> existing = inFlight.putIfAbsent(key, mine);

        if (existing == null) {
            try {
                FastBlendChunk generated = generator.get();
                put(key, generated);
                mine.complete(generated);
                return generated;
            } catch (Throwable t) {
                mine.completeExceptionally(t);
                throw t;
            } finally {
                inFlight.remove(key, mine);
            }
        }

        return existing.join();
    }

    public void prefetch(long key, Supplier<FastBlendChunk> generator) {
        lock.lock();
        try {
            if (cache.containsKey(key)) {
                return;
            }
        } finally {
            lock.unlock();
        }

        CompletableFuture<FastBlendChunk> mine = new CompletableFuture<>();
        CompletableFuture<FastBlendChunk> existing = inFlight.putIfAbsent(key, mine);

        if (existing != null) {
            return;
        }

        try {
            FastBlendChunk generated = generator.get();
            put(key, generated);
            mine.complete(generated);
        } catch (Throwable t) {
            mine.completeExceptionally(t);
        } finally {
            inFlight.remove(key, mine);
        }
    }

    public void put(long key, FastBlendChunk chunk) {
        lock.lock();
        try {
            FastBlendChunk old = cache.remove(key);
            if (old != null) {
                unregisterXZNoLock(key);
            }

            chunk.key = key;
            cache.putAndMoveToFirst(key, chunk);
            registerXZNoLock(key);
            trimToSizeNoLock();
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            cache.clear();
            byXZ.clear();
            epoch++;
        } finally {
            lock.unlock();
        }
    }

    public void resize(int newMaxEntries) {
        lock.lock();
        try {
            if (newMaxEntries < 64) {
                newMaxEntries = 64;
            }

            if (this.maxEntries != newMaxEntries) {
                this.maxEntries = newMaxEntries;
                trimToSizeNoLock();
                epoch++;
            }
        } finally {
            lock.unlock();
        }
    }

    public void invalidateChunkNeighborhood(int chunkX, int chunkZ, int radius) {
        lock.lock();
        try {
            boolean changed = false;

            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    long xz = packXZKey(chunkX + dx, chunkZ + dz);
                    LongArrayList keys = byXZ.remove(xz);
                    if (keys == null) {
                        continue;
                    }

                    changed = true;
                    for (int i = 0; i < keys.size(); i++) {
                        long fullKey = keys.getLong(i);
                        cache.remove(fullKey);
                    }
                }
            }

            if (changed) {
                epoch++;
            }
        } finally {
            lock.unlock();
        }
    }

    public void invalidateChunkNeighborhood(int chunkX, int chunkZ) {
        invalidateChunkNeighborhood(chunkX, chunkZ, 1);
    }

    private void trimToSizeNoLock() {
        while (cache.size() > maxEntries) {
            long lastKey = cache.lastLongKey();
            cache.removeLast();
            unregisterXZNoLock(lastKey);
        }
    }

    private void registerXZNoLock(long fullKey) {
        long xz = packXZKey(unpackChunkX(fullKey), unpackChunkZ(fullKey));
        LongArrayList keys = byXZ.get(xz);
        if (keys == null) {
            keys = new LongArrayList(4);
            byXZ.put(xz, keys);
        }
        keys.add(fullKey);
    }

    private void unregisterXZNoLock(long fullKey) {
        long xz = packXZKey(unpackChunkX(fullKey), unpackChunkZ(fullKey));
        LongArrayList keys = byXZ.get(xz);
        if (keys == null) {
            return;
        }

        for (int i = keys.size() - 1; i >= 0; i--) {
            if (keys.getLong(i) == fullKey) {
                keys.removeLong(i);
                break;
            }
        }

        if (keys.isEmpty()) {
            byXZ.remove(xz);
        }
    }

    private static long packXZKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) ^ (chunkZ & 0xFFFFFFFFL);
    }

    public static long packKey(int chunkX, int chunkY, int chunkZ, int colorType) {
        long x = ((long) chunkX & 0x3FFFFFL);
        long y = ((long) chunkY & 0xFFFFFL);
        long z = ((long) chunkZ & 0x3FFFFFL);
        long t = ((long) colorType & 0xFL);

        return x | (z << 22) | (y << 44) | (t << 60);
    }

    public static int unpackChunkX(long key) {
        int v = (int) (key & 0x3FFFFF);
        return (v << 10) >> 10;
    }

    public static int unpackChunkZ(long key) {
        int v = (int) ((key >>> 22) & 0x3FFFFF);
        return (v << 10) >> 10;
    }
}