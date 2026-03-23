package UD.CabalCore.BBB;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.ColorResolver;

import java.util.IdentityHashMap;

public final class ResolverIDs {
    private static final IdentityHashMap<ColorResolver, Integer> IDS = new IdentityHashMap<>();
    private static int nextId = 3;

    static {
        IDS.put(BiomeColors.GRASS_COLOR_RESOLVER, 0);
        IDS.put(BiomeColors.WATER_COLOR_RESOLVER, 1);
        IDS.put(BiomeColors.FOLIAGE_COLOR_RESOLVER, 2);
    }

    private ResolverIDs() {
    }

    public static synchronized int getId(ColorResolver resolver) {
        Integer id = IDS.get(resolver);
        if (id != null) {
            return id;
        }

        int created = nextId++;
        IDS.put(resolver, created);
        return created;
    }
}