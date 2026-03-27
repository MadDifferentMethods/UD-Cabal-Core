package UD.CabalCore.mixin.ReVision;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class ZNVS {

    @Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
    private static void cc$noNightVisionScale(LivingEntity entity, float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.0F);
    }
}