package UD.CabalCore.DeltaLayer.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue ENABLED_VALUE;
    private static final ForgeConfigSpec.IntValue LOD_DISTANCE_VALUE;
    private static final ForgeConfigSpec.BooleanValue RENDER_SELF_VALUE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("delta_layer");

        ENABLED_VALUE = builder
                .comment("Enable Delta Layer 3D outer skin rendering.")
                .define("enabled", true);

        LOD_DISTANCE_VALUE = builder
                .comment("3D skin layer render distance in blocks.")
                .defineInRange("lodDistance", 12, 0, 128);

        RENDER_SELF_VALUE = builder
                .comment("Render Delta Layer on the local player in third person.")
                .define("renderSelf", true);

        builder.pop();

        SPEC = builder.build();
    }

    private Config() {
    }

    public static boolean isEnabled() {
        return ENABLED_VALUE.get();
    }

    public static int getLodDistance() {
        return LOD_DISTANCE_VALUE.get();
    }

    public static boolean renderSelf() {
        return RENDER_SELF_VALUE.get();
    }
}