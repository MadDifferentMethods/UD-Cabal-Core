package UD.CabalCore.DeltaLayer.mesh;

import UD.CabalCore.DeltaLayer.cache.MeshKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class Cache {
    private static final Map<MeshKey, MeshBundle> CACHE = new ConcurrentHashMap<>();

    private Cache() {
    }

    public static MeshBundle getOrBuild(MeshKey key, Supplier<MeshBundle> builder) {
        return CACHE.computeIfAbsent(key, ignored -> builder.get());
    }

    public static void invalidate(MeshKey key) {
        CACHE.remove(key);
    }

    public static void clear() {
        CACHE.clear();
    }

    public static int size() {
        return CACHE.size();
    }
}