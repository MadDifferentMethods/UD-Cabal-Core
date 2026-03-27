package UD.CabalCore.mixin.ReVision;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class ZWV {

    @Inject(method = "getWaterVision", at = @At("HEAD"), cancellable = true)
    private void cc$noWaterVision(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.0F);
    }
}