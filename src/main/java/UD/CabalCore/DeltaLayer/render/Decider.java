package UD.CabalCore.DeltaLayer.render;

import UD.CabalCore.DeltaLayer.config.Config;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

public final class Decider {

    private Decider() {
    }

    public static boolean shouldRender(AbstractClientPlayer player) {
        if (!Config.SPEC.isLoaded()) {
            return false;
        }

        if (!Config.isEnabled()) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) {
            return false;
        }

        double dx = player.getX() - camera.getPosition().x;
        double dy = player.getY() - camera.getPosition().y;
        double dz = player.getZ() - camera.getPosition().z;

        int lod = Config.getLodDistance();
        int lodSq = lod * lod;

        return dx * dx + dy * dy + dz * dz <= lodSq;
    }
}