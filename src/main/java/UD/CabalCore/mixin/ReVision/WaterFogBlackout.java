package UD.CabalCore.mixin.ReVision;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class WaterFogBlackout {

    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void cc$darkenUnderwaterFog(Camera camera,
                                               float partialTick,
                                               net.minecraft.client.multiplayer.ClientLevel level,
                                               int renderDistance,
                                               float bossColorModifier,
                                               CallbackInfo ci) {
        if (camera.getFluidInCamera() == FogType.WATER) {
            fogRed *= 0.08F;
            fogGreen *= 0.08F;
            fogBlue *= 0.10F;
        }
    }
}