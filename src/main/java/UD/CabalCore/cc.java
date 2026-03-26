package UD.CabalCore;

import UD.CabalCore.DeltaLayer.DeltaLayer;
import UD.CabalCore.DeltaLayer.config.Config;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(cc.MODID)
public class cc {
    public static final String MODID = "cc";

    @SuppressWarnings("removal")
    public cc() {

    }
}


////Deactivated
//ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
//        DeltaLayer.init();;