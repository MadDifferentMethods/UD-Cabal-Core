package UD.CabalCore.mixin.ReVision;

import UD.CabalCore.ReVision.EyeAdapt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LightTexture.class, priority = 1500)
public abstract class EyeUpdateHook {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "updateLightTexture", at = @At("HEAD"))
    private void cc$updateEyeAdapt(float partialTick, CallbackInfo ci) {
        EyeAdapt.update(this.minecraft, partialTick);
    }
}