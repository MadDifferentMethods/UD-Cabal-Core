package UD.CabalCore.DeltaLayer.render;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class LayerRegistrar {

    private LayerRegistrar() {
    }

    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinName : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skinName);
            if (renderer != null) {
                renderer.addLayer(new LayerRender<>(renderer));
                System.out.println("[DeltaLayer] adding player render layers");
            }
        }
    }
}