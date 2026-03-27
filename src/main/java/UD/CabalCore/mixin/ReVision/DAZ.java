package UD.CabalCore.mixin.ReVision;


import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionSpecialEffects.class)
public abstract class DAZ {

    @Inject(method = "forceBrightLightmap", at = @At("HEAD"), cancellable = true)
    private void cc$noForceBrightLightmap(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "constantAmbientLight", at = @At("HEAD"), cancellable = true)
    private void cc$noConstantAmbientLight(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}