package UD.CabalCore.mixin.Opt;

public final class Flags {
    private Flags() {
    }

    /*
     * Hard removals / nullers
     */
    public static final boolean TOAST_OBLITERATOR = true;
    public static final boolean TUTORIAL_NULLER = true;

    /*
     * Safe hot-path trims
     */
    public static final boolean PARTICLE_VOID = true;
    public static final boolean FOV_ZERO_BYPASS = true;

    /*
     * Debug renderer nullers
     */
    public static final boolean DEBUG_GAMETEST_NULLER = true;
    public static final boolean DEBUG_GAMEEVENT_NULLER = true;
    public static final boolean DEBUG_BEE_NULLER = true;
    public static final boolean DEBUG_BRAIN_NULLER = true;

    /*
     * Phase 2
     */
    public static final boolean LIGHTMAP_INVALIDATOR = true;

    /*
     * Phase 3
     */

    public static final boolean SKY_COLOR_CACHE = true;
}
