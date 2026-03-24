package UD.CabalCore.DeltaLayer;

import UD.CabalCore.DeltaLayer.config.Config;
import UD.CabalCore.DeltaLayer.render.LayerRegistrar;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class DeltaLayer {

    private DeltaLayer() {
    }
    @SuppressWarnings("removal")
    public static void init() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(DeltaLayer::onClientSetup);
        modBus.addListener(LayerRegistrar::onAddLayers);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        // nothing for now
    }
}