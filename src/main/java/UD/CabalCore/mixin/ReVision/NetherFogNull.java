package UD.CabalCore.mixin.ReVision;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class NetherFogNull {

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void cc$killNetherFog(Camera camera,
                                         FogRenderer.FogMode fogMode,
                                         float farPlaneDistance,
                                         boolean nearFog,
                                         float partialTick,
                                         CallbackInfo ci) {
        if (camera.getFluidInCamera() == FogType.NONE) {
            ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
            if (level != null && level.dimension() == Level.NETHER) {
                com.mojang.blaze3d.systems.RenderSystem.setShaderFogStart(Float.MAX_VALUE);
                com.mojang.blaze3d.systems.RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
                com.mojang.blaze3d.systems.RenderSystem.setShaderFogShape(FogShape.SPHERE);
                ci.cancel();
            }
        }
    }
}