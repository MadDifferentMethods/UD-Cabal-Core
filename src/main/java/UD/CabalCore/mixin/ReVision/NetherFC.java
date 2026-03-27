package UD.CabalCore.mixin.ReVision;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionSpecialEffects.NetherEffects.class)
public abstract class NetherFC {

    @Inject(method = "getBrightnessDependentFogColor", at = @At("HEAD"), cancellable = true)
    private void cc$darkNetherFog(Vec3 color, float brightness, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(new Vec3(
                color.x * 0.08D,
                color.y * 0.03D,
                color.z * 0.03D
        ));
    }
}