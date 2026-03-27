package UD.CabalCore.mixin.ReVision;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.DimensionSpecialEffects$EndEffects")
public abstract class EndFC {

    @Inject(method = "getBrightnessDependentFogColor", at = @At("HEAD"), cancellable = true)
    private void cc$darkEndFog(Vec3 color, float brightness, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(new Vec3(
                color.x * 0.05D,
                color.y * 0.05D,
                color.z * 0.07D
        ));
    }
}